package com.szr.flashim.gateway.tcp.config;

import com.szr.flashim.core.netty.event.NettyEventExecutor;
import com.szr.flashim.core.netty.thread.ThreadFactoryImpl;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(GatewayNettyServerConfig.class)
public class NettyServerConfiguration {

    @Bean
    public EventExecutorGroup defaultEventExecutorGroup(GatewayNettyServerConfig nettyConfig) {
        return new DefaultEventExecutorGroup(nettyConfig.getServerHandlerThreads(),
                new ThreadFactoryImpl("NettyServerHandlerThread_"));
    }

    @Bean
    public NettyEventExecutor nettyEventExecutor() {
        return new NettyEventExecutor();
    }

    @Bean
    public ExecutorService publicExecutor() {
        return Executors.newFixedThreadPool(4,
                new ThreadFactoryImpl("NettyServerPublicExecutor_"));
    }

    @Bean
    public ExecutorService callBackExecutor() {
        return Executors.newFixedThreadPool(4,
                new ThreadFactoryImpl("NettyServerCallBackExecutor_"));
    }

    @Bean
    public ExecutorService bizProcessorExecutor() {
        return Executors.newFixedThreadPool(4,
                new ThreadFactoryImpl("NettyServerBizProcessorExecutor_"));
    }
}
