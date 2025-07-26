package com.flashim.szr.cache.starter;

import com.szr.flashim.general.exception.ServiceException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

// 严格有序的ID生成器
public class SequenceIdGenerator {
    private final RedisTemplate<String, Long> redisTemplate;

    public SequenceIdGenerator(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 生成会话内严格有序ID（带重试机制）
    public Long generateOrderSeq(String sessionId) {
        String redisKey = CacheConstant.CHAT_SESSION_SEQ + "{" + sessionId + "}"; // {}保证集群同节点

        // 重试3次（应对Redis短暂抖动）
        for (int i = 0; i < 3; i++) {
            try {
                return redisTemplate.opsForValue().increment(redisKey, 1L);
            } catch (RedisConnectionFailureException e) {
                try {
                    Thread.sleep(10 * (i + 1)); // 指数退避
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new ServiceException("ID生成失败，请降级处理");
    }
}