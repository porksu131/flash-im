package com.flashim.szr.cache.starter;

import com.szr.flashim.general.constant.OnlineStatus;
import io.netty.channel.Channel;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class UserSessionManager {
    private RedisTemplate<String, Map<String, Object>> mapRedisTemplate;
    private RedisTemplate<String, Object> redisTemplate;

    public UserSessionManager(RedisTemplate<String, Map<String, Object>> mapRedisTemplate,
                              RedisTemplate<String, Object> redisTemplate) {
        this.mapRedisTemplate = mapRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    private static final Map<String, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public void addLocalSession(String address, Channel value) {
        CHANNEL_CACHE.put(address, value);
    }

    public Channel getLocalSession(String address) {
        return CHANNEL_CACHE.get(address);
    }

    public void removeLocalSession(String address) {
        CHANNEL_CACHE.remove(address);
    }

    public void saveLocalSession(String address, Channel channel) {
        addLocalSession(address, channel);
    }

    public void addRedisSession(String key, Map<String, Object> value) {
        mapRedisTemplate.opsForValue().set(key, value, 7, TimeUnit.DAYS);
    }

    public void removeRedisSession(String key) {
        mapRedisTemplate.delete(key);
    }

    public Map<String, Object> getRedisSession(String key) {
        return mapRedisTemplate.opsForValue().get(key);
    }

    public Map<String, Object> getUserRedisSession(Long userId) {
        return mapRedisTemplate.opsForValue().get(CacheConstant.USER_GATEWAY_SERVER + userId);
    }

    public void addUserRedisSession(Long userId, Map<String, Object> value) {
        mapRedisTemplate.opsForValue().set(CacheConstant.USER_GATEWAY_SERVER + userId, value, 7, TimeUnit.DAYS);
    }

    public void removeUserRedisSession(Long userId) {
        mapRedisTemplate.delete(CacheConstant.USER_GATEWAY_SERVER + userId);
    }

    // 设置用户在线状态
    public void setUserOnline(Long userId) {
        redisTemplate.opsForValue().set(CacheConstant.USER_ONLINE_STATUS + userId, OnlineStatus.ONLINE, 90, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(CacheConstant.USER_ACTIVE_TIME + userId, System.currentTimeMillis());
    }

    // 设置用户离线状态
    public void setUserOffline(Long userId) {
        redisTemplate.opsForValue().set(CacheConstant.USER_ONLINE_STATUS + userId, OnlineStatus.OFFLINE);
    }
}
