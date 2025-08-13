package com.flashim.szr.cache.starter;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.util.annotation.NonNullApi;

import java.util.concurrent.TimeUnit;


public class RedisWorkerIdAssigner {
    private static final String WORKER_ID_KEY = CacheConstant.SNOWFLAKE_WORKER_ID;
    private final RedisTemplate<String, Long> redisTemplate;
    private final Long workerId;

    public RedisWorkerIdAssigner(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.workerId = assignWorkerId();
    }

    private Long assignWorkerId() {
        // 尝试获取已有workerId
        Long existingId = redisTemplate.opsForValue().get(WORKER_ID_KEY + ":current");
        if (existingId != null) {
            return existingId;
        }
        // 原子性分配新ID
        return redisTemplate.execute(new SessionCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public Long execute(RedisOperations operations) {
                operations.watch(WORKER_ID_KEY);

                // 获取当前最大ID
                Long maxId = operations.opsForValue().increment(WORKER_ID_KEY, 1L);
                if (maxId == null) {
                    maxId = 0L;
                    operations.opsForValue().set(WORKER_ID_KEY, maxId);
                }

                // 限制ID范围 (0-1023)
                long assignedId = maxId % 1024;
                operations.multi();
                operations.opsForValue().set(
                        WORKER_ID_KEY + ":" + assignedId,
                        System.currentTimeMillis(),
                        30, TimeUnit.SECONDS // 设置30秒过期（防止宕机占用）
                );
                operations.exec();

                return assignedId;
            }
        });

    }

    // 定时续期任务
    @Scheduled(fixedRate = 10000)
    public void renewWorkerIdLease() {
        if (workerId != null) {
            redisTemplate.expire(
                    WORKER_ID_KEY + ":" + workerId,
                    30, TimeUnit.SECONDS
            );
        }
    }

    public Long getWorkerId() {
        return workerId;
    }
}