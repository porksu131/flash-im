package com.szr.flashim.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.szr.flashim.auth.mapper")
public class FlashImAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashImAuthApplication.class, args);
    }
}
