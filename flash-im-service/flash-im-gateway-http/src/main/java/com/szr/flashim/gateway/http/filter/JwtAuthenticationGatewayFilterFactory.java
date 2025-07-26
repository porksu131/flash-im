package com.szr.flashim.gateway.http.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.szr.flashim.gateway.http.util.DurationParser;
import com.szr.flashim.security.starter.JwtProperties;
import com.szr.flashim.security.starter.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.util.Map;

@Component
public class JwtAuthenticationGatewayFilterFactory extends
        AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilterFactory.class);
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final WebClient webClient;

    public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil,
                                                 JwtProperties jwtProperties,
                                                 WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.webClient = webClientBuilder.baseUrl(jwtProperties.getAuthCenterUrl()).build();
    }

    @PostConstruct
    public void init() {
        loadPublicKeyWithRetry();
    }

    // 带重试机制的加载逻辑（可选）
    private void loadPublicKeyWithRetry() {
        int maxAttempts = 3;
        int attempt = 0;
        long backoff = 2000; // 2秒初始退避

        while (attempt < maxAttempts) {
            try {
                loadPublicKey();
                return;
            } catch (Exception e) {
                attempt++;
                log.warn("Public key fetch attempt {} failed", attempt, e);
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(backoff);
                        backoff *= 2; // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        throw new RuntimeException("Failed to fetch public key after " + maxAttempts + " attempts");
    }

    private void loadPublicKey() {
        try {
            log.info("Loading public key from auth center...");
            String publicKeyPem = "";
            String keyResult = webClient.get()
                    .uri("/auth/publicKey")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException(
                                            "Failed to fetch public key: " + response.statusCode() + " - " + error)))
                    )
                    .bodyToMono(String.class)
                    .block(DurationParser.parse(jwtProperties.getKeyFetchTimeout()));
            Map<String, Object> keyResultMap = new ObjectMapper().readValue(keyResult, Map.class);
            if (keyResultMap != null && 200 == (Integer) keyResultMap.get("code")) {
                publicKeyPem = (String) keyResultMap.get("data");
            }

            PublicKey publicKey = jwtUtil.getPublicKey(publicKeyPem);
            jwtUtil.setPublicKey(publicKey);
            log.info("Public key loaded successfully");
        } catch (Exception e) {
            log.error("Critical: Failed to load public key on startup", e);
            throw new IllegalStateException("Failed to load public key on startup", e);
        }
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            try {
                // 从请求头中获取 Token
                String token = request.getHeaders().getFirst("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                if (!StringUtils.hasText(token)) {
                    throw new RuntimeException("token is empty!");
                }
                // 校验 Token
                Claims claims = jwtUtil.validateAndExtractClaims(token);
                // 提取 payload 并放到 header 中
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("user", new ObjectMapper().writeValueAsString(claims))
                        .build();
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                // 校验失败，返回 401
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                String jsonResponse = "{\"code\": 401, \"msg\": \"token invalid\", \"data\": null}";
                return response.writeWith(
                        Mono.just(response.bufferFactory().wrap(jsonResponse.getBytes())));
            }
        };
    }

    public static class Config {
        // 如果需要配置参数，可以在这里定义
    }
}