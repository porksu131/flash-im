package com.szr.flashim.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String chatBucket;
    private Long defaultPartSize;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getChatBucket() {
        return chatBucket;
    }

    public void setChatBucket(String chatBucket) {
        this.chatBucket = chatBucket;
    }

    public Long getDefaultPartSize() {
        return defaultPartSize;
    }

    public void setDefaultPartSize(Long defaultPartSize) {
        this.defaultPartSize = defaultPartSize;
    }
}
