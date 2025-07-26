package com.szr.flashim.security.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flashim.jwt")
public class JwtProperties {
    private Boolean enabled;
    private String privateKeyPath;
    private String publicKeyPath;
    private long accessTokenExpiration; // ms
    private long refreshTokenExpiration; // ms
    private long refreshThreshold; // 刷新阈值(ms)
    private String authCenterUrl; // 授权中心的基础地址
    private String keyFetchTimeout; // 每次拉取公钥超时时间
    private int keyFetchMaxRetries; // 拉取公钥最大重试次数


    public int getKeyFetchMaxRetries() {
        return keyFetchMaxRetries;
    }

    public void setKeyFetchMaxRetries(int keyFetchMaxRetries) {
        this.keyFetchMaxRetries = keyFetchMaxRetries;
    }

    public String getKeyFetchTimeout() {
        return keyFetchTimeout;
    }

    public void setKeyFetchTimeout(String keyFetchTimeout) {
        this.keyFetchTimeout = keyFetchTimeout;
    }

    public String getAuthCenterUrl() {
        return authCenterUrl;
    }

    public void setAuthCenterUrl(String authCenterUrl) {
        this.authCenterUrl = authCenterUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public long getRefreshThreshold() {
        return refreshThreshold;
    }

    public void setRefreshThreshold(long refreshThreshold) {
        this.refreshThreshold = refreshThreshold;
    }
}