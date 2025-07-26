package com.szr.flashim.gateway.tcp.netty.service;

import com.szr.flashim.gateway.tcp.remote.LoadBalancedHttpClient;
import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.security.starter.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.security.PublicKey;

@Service
public class AuthorizeServiceImpl implements AuthorizeService, CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizeServiceImpl.class);
    private final LoadBalancedHttpClient httpClient;
    private final JwtUtil jwtUtil;

    public AuthorizeServiceImpl(LoadBalancedHttpClient httpClient, JwtUtil jwtUtil) {
        this.httpClient = httpClient;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean isAuth(long uid, String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            ResponseResult<String> result = getPublicKey();
            if (ResponseResult.isSuccess(result)) {
                PublicKey publicKey = jwtUtil.getPublicKey(result.getData());
                jwtUtil.setPublicKey(publicKey);
            } else {
                LOGGER.error("请求jwt公钥异常：{}", result.getMsg());
                throw new RuntimeException("请求jwt公钥异常：" + result.getMsg());
            }
        } catch (Exception e) {
            LOGGER.error("加载jwt公钥异常：{}", e.getMessage());
            throw new RuntimeException("加载jwt公钥异常：" + e.getMessage());
        }
    }

    // spring容器启动完成后 从认证中心获取公钥
    public ResponseResult<String> getPublicKey() {
        // 后续需要改为url可配置
        return httpClient.get(
                "flash-im-auth",
                "/auth/publicKey",
                null,
                String.class
        );
    }
}
