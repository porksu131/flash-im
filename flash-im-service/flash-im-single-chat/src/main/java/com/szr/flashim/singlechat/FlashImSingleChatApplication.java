package com.szr.flashim.singlechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.szr.flashim.singlechat.mapper")
@EnableDiscoveryClient
public class FlashImSingleChatApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(FlashImSingleChatApplication.class, args);
    }
}
