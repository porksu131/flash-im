package com.szr.flashim.offline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncConfig {

    // 配置离线消息处理的专用线程池
    @Bean(name = "offlineMessageExecutor")
    public Executor offlineMessageExecutor(OfflineAsyncProperties offlineAsyncProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数（长期保持的线程数）
        executor.setCorePoolSize(offlineAsyncProperties.getCorePoolSize());

        // 最大线程数（最大可创建的线程数）
        executor.setMaxPoolSize(offlineAsyncProperties.getMaxPoolSize());

        // 队列容量（等待执行的任务队列大小）
        executor.setQueueCapacity(offlineAsyncProperties.getQueueCapacity());

        // 线程名前缀（便于日志追踪）
        executor.setThreadNamePrefix("OfflineMsg-");

        // 拒绝策略（当线程池饱和时的处理方式）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 线程空闲时间（超过核心线程数的线程空闲多久后被回收）
        executor.setKeepAliveSeconds(60);

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 应用关闭时等待任务完成的超时时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    // 可选：配置默认线程池（用于其他异步任务）
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("DefaultAsync-");
        executor.initialize();
        return executor;
    }
}