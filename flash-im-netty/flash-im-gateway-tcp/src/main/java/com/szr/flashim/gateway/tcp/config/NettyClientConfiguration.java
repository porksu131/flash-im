package com.szr.flashim.gateway.tcp.config;

import com.szr.flashim.core.netty.processor.DefaultAuthNotifyProcessor;
import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.core.netty.processor.NettyReceiveAuthNotifyInvoke;
import com.szr.flashim.core.netty.thread.ThreadFactoryImpl;
import com.szr.flashim.gateway.tcp.netty.client.manager.ConnectionManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(GatewayNettyClientConfig.class)
public class NettyClientConfiguration {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 线程池大小
        scheduler.setThreadNamePrefix("connect-scheduled-task-"); // 线程名前缀
        scheduler.setAwaitTerminationSeconds(60); // 关闭时等待任务完成
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 优雅关闭
        return scheduler;
    }

    @Bean
    ExecutorService connectionExecutor() {
        return Executors.newFixedThreadPool(1,
                new ThreadFactoryImpl("NettyClientConnectExecutor_"));
    }

    @Bean
    public ConnectionManager connectionManager(
            GatewayNettyClientConfig gatewayNettyClientConfig,
            ThreadPoolTaskScheduler taskScheduler) {
        return new ConnectionManager(gatewayNettyClientConfig, taskScheduler);
    }
}
