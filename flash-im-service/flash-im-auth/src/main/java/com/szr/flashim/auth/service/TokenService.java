package com.szr.flashim.auth.service;

import com.flashim.szr.cache.starter.CacheConstant;
import com.szr.flashim.auth.model.CustomUserDetails;
import com.szr.flashim.security.starter.JwtProperties;
import com.szr.flashim.security.starter.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public TokenService(RedisTemplate<String, String> redisTemplate,
                        JwtUtil jwtUtil,
                        JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 保存 refreshToken 到 redis，过期时间设置长一点
     *
     * @param userId       用户id
     * @param refreshToken refreshToken
     */
    public void storeRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                CacheConstant.JWT_REFRESH_TOKEN + userId,
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 刷新 accessToken
     *
     * @param refreshToken refreshToken
     * @return newAccessToken
     */
    public String refreshAccessToken(String refreshToken) {
        Claims claims = jwtUtil.validateAndExtractClaims(refreshToken);
        String userId = claims.getSubject();

        // 验证Redis中的刷新令牌是否匹配
        String storedRefreshToken = redisTemplate.opsForValue().get(CacheConstant.JWT_REFRESH_TOKEN + userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 更新用户的权限信息
        claims.put("authorities", new ArrayList<>()); /* todo 从数据库加载权限 */

        // 生成新令牌
        return jwtUtil.generateAccessToken(claims);
    }

    /**
     * 保存 accessToken 黑名单
     *
     * @param accessToken accessToken
     */
    public void blacklistToken(String accessToken) {
        try {
            Claims claims = jwtUtil.validateAndExtractClaims(accessToken);
            long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();

            if (expiration > 0) {
                redisTemplate.opsForValue().set(
                        CacheConstant.JWT_BLACK_LIST + claims.getId(),
                        "invalid",
                        expiration,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            // 处理无效令牌，本身就是做登出，此处无须处理
        }
    }

    /**
     * 判断是否在黑名单内
     *
     * @param accessToken accessToken
     * @return true:存在，false:不存在
     */
    public boolean isBlacklisted(String accessToken) {
        try {
            Claims claims = jwtUtil.validateAndExtractClaims(accessToken);
            return redisTemplate.hasKey(CacheConstant.JWT_BLACK_LIST + claims.getId());
        } catch (Exception e) {
            return true; // 无效令牌视为黑名单
        }
    }
}
