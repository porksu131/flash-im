package com.szr.flashim.gateway.tcp.netty.client.manager;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.szr.flashim.gateway.tcp.config.GatewayNettyClientConfig;
import io.netty.channel.Channel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
    private final GatewayNettyClientConfig gateWayNettyClientConfig;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<String, ReconnectContext> reconnectContexts = new ConcurrentHashMap<>();
    private final Map<String, Channel> activeConnections = new ConcurrentHashMap<>();

    private final List<String> currentInstances = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);


    public ConnectionManager(
            GatewayNettyClientConfig gateWayNettyClientConfig,
            ThreadPoolTaskScheduler taskScheduler) {
        this.gateWayNettyClientConfig = gateWayNettyClientConfig;
        this.taskScheduler = taskScheduler;
    }

    public void addConnection(Instance instance, Channel channel) {
        String address = getAddress(instance);
        activeConnections.put(address, channel);
        currentInstances.add(address);
    }

    public void removeConnection(String address) {
        Channel channel = activeConnections.remove(address);
        if (channel != null) {
            channel.close();
        }
        currentInstances.remove(address);
    }

    public Channel getConnectionChannel(String address) {
        return activeConnections.get(address);
    }

    public boolean hasConnection(String address) {
        return activeConnections.containsKey(address);
    }

    public Map<String, Channel> getAllConnections() {
        return new ConcurrentHashMap<>(activeConnections);
    }

    public boolean isActive(String addr) {
        Channel channel = activeConnections.get(addr);
        return channel != null && channel.isActive();
    }

    public void closeAllConnections() {
        activeConnections.values().forEach(channel -> channel.close().awaitUninterruptibly());
        activeConnections.clear();
        currentInstances.clear();
    }

    public String getAddress(Instance instance) {
        return instance.getIp() + ":" + instance.getPort();
    }

    public void scheduleReconnect(Instance instance, Runnable connectTask) {
        String address = getAddress(instance);
        ReconnectContext context = reconnectContexts.computeIfAbsent(
                address, k -> new ReconnectContext(this.gateWayNettyClientConfig, instance)
        );

        int reconnectMaxAttempts = gateWayNettyClientConfig.getReconnectMaxAttempts();
        if (!context.shouldReconnect()) {
            LOGGER.warn("达到最大重连次数[{}], 放弃连接: {}", reconnectMaxAttempts, address);
            reconnectContexts.remove(address);
            return;
        }

        long delay = context.getAndUpdateDelay();
        LOGGER.info("调度重连: {} (尝试 {}/{}, 延迟 {}ms)", address, context.attemptCount, reconnectMaxAttempts, delay);

        // 延迟任务
        ScheduledFuture<?> future = taskScheduler.schedule(
                connectTask,
                java.time.Instant.now().plusMillis(delay)
        );
        context.setScheduledFuture(future);
    }

    public void resetReconnectContext(String address) {
        ReconnectContext context = reconnectContexts.get(address);
        if (context != null) {
            context.reset();
        }
    }


    public String selectOneByRoundRobin() {
        if (currentInstances.isEmpty()) {
            throw new IllegalStateException("No instances available");
        }

        // 处理索引溢出（重置为0）
        int index = currentIndex.getAndUpdate(i ->
                (i >= Integer.MAX_VALUE - 1) ? 0 : i + 1
        ) % currentInstances.size();

        return currentInstances.get(Math.abs(index));
    }

    public String selectOneRandom() {
        if (currentInstances.isEmpty()) {
            throw new IllegalStateException("No instances available");
        }
        int index = ThreadLocalRandom.current().nextInt(currentInstances.size());
        return currentInstances.get(index);
    }

    @PreDestroy
    public void shutdown() {
        taskScheduler.shutdown();
    }
}
