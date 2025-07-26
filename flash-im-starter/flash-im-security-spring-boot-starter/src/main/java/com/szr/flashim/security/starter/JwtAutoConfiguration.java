package com.szr.flashim.security.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(name = "flashim.jwt.enabled")
public class JwtAutoConfiguration {
    @Bean
    JwtUtil jwtUtil(JwtProperties jwtProperties, ResourceLoader resourceLoader) {
        return new JwtUtil(jwtProperties, resourceLoader);
    }
}
