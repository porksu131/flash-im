package com.szr.flashim.security.starter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class JwtUtil {
    private final JwtProperties jwtProperties;
    private final ResourceLoader resourceLoader;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private String publicKeyStr;

    public JwtUtil(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        this.jwtProperties = jwtProperties;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        if (StringUtils.isNotBlank(jwtProperties.getPrivateKeyPath())) {
            this.privateKey = loadPrivateKey(jwtProperties.getPrivateKeyPath());
        }
        if (StringUtils.isNotBlank(jwtProperties.getPrivateKeyPath())) {
            this.publicKey = loadPublicKey(jwtProperties.getPublicKeyPath());
        }
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    // 生成访问令牌
    public String generateAccessToken(Map<String, Object> claims) {
        return buildToken(claims, claims.get("userName").toString(), jwtProperties.getAccessTokenExpiration());
    }

    // 生成刷新令牌
    public String generateRefreshToken(Map<String, Object> claims) {
        return buildToken(claims, claims.get("userName").toString(), jwtProperties.getRefreshTokenExpiration());
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setId(UUID.randomUUID().toString()) // JTI
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // 验证JWT并返回声明
    public Claims validateAndExtractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 验证JWT并返回声明
    public Claims validateAndExtractClaims(String token, String publicKeyStr) throws Exception {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey(publicKeyStr))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 检查令牌是否即将过期（用于滑动会话）
    public boolean isTokenNearExpiration(Claims claims) {
        try {
            Date expiration = claims.getExpiration();
            return (expiration.getTime() - System.currentTimeMillis()) < jwtProperties.getRefreshThreshold();
        } catch (Exception e) {
            return false;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = validateAndExtractClaims(token);
        return claimsResolver.apply(claims);
    }


    // 加载私钥
    public PrivateKey loadPrivateKey(String path) throws Exception {
        String privateKeyStr = loadKeyStr(path);
        return getPrivateKey(privateKeyStr);
    }

    // 加载公钥
    public PublicKey loadPublicKey(String path) throws Exception {
        this.publicKeyStr = loadKeyStr(path);
        return getPublicKey(this.publicKeyStr);
    }

    // 从Base64字符串加载RSA私钥
    public PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    // 从Base64字符串加载RSA公钥
    public PublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private String loadKeyStr(String path) throws Exception {
        Resource resource = resourceLoader.getResource(path);
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            String keyPem = FileCopyUtils.copyToString(reader);
            // 清除PEM文件中的标记
            return keyPem
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s+", "");
        }
    }

    public String getPublicKeyPem() {
        return publicKeyStr;
    }

}
