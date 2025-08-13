package com.szr.flashim.file.service;

import com.szr.flashim.file.config.MinioProperties;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MinioService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioService.class);
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }


    @PostConstruct
    public void init() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getChatBucket()).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getChatBucket()).build());
            }
        } catch (Exception e) {
            LOGGER.error("init minio bucket error :{}", e.getMessage(), e);
        }

    }

    /**
     * 获取上传URL(预签名)
     */
    public String getPreSignedPutObjectUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("get minio preSigned upload url error", e);
        }
    }

    /**
     * 获取下载URL(预签名)
     */
    public String getPreSignedGetObjectUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("get minio preSigned download url error", e);
        }
    }

    /**
     * 合并对象
     */
    public void composeObject(String bucketName, String targetObject, List<String> sourceObjects)
            throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        List<ComposeSource> sourceList = new ArrayList<>(sourceObjects.size());
        for (String sourceObject : sourceObjects) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucketName)
                    .object(sourceObject)
                    .build();
            sourceList.add(composeSource);
        }

        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(bucketName)
                        .object(targetObject)
                        .sources(sourceList)
                        .build());
    }

    /**
     * 删除对象
     */
    public void removeObject(String bucketName, String objectName)
            throws ServerException, InsufficientDataException, ErrorResponseException,
            IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }
}