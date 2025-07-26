package com.szr.flashim.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.szr.flashim"})
@EnableDiscoveryClient
public class FlashImDispatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlashImDispatchApplication.class, args);
    }
}
