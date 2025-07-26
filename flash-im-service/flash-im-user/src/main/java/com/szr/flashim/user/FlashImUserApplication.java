package com.szr.flashim.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.szr.flashim.user.mapper")
@EnableDiscoveryClient
public class FlashImUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashImUserApplication.class, args);
    }
}
