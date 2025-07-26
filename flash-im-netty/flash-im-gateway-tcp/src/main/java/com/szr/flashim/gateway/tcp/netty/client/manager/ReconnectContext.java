package com.szr.flashim.gateway.tcp.netty.client.manager;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.szr.flashim.gateway.tcp.config.GatewayNettyClientConfig;

import java.util.concurrent.ScheduledFuture;

public class ReconnectContext {
    final GatewayNettyClientConfig properties;
    final Instance instance;
    int attemptCount = 0;
    long nextDelay;
    ScheduledFuture<?> scheduledFuture;

    public ReconnectContext(GatewayNettyClientConfig properties, Instance instance) {
        this.properties = properties;
        this.instance = instance;
    }


    void reset() {
        attemptCount = 0;
        nextDelay = properties.getReconnectInitialDelay();
        cancelScheduledTask();
    }

    boolean shouldReconnect() {
        return attemptCount < properties.getReconnectMaxAttempts();
    }

    long getAndUpdateDelay() {
        long currentDelay = nextDelay;
        nextDelay = (long) Math.min(
                currentDelay * properties.getReconnectMultiplier(),
                properties.getReconnectMaxDelay()
        );
        attemptCount++;
        return currentDelay;
    }

    void setScheduledFuture(ScheduledFuture<?> future) {
        cancelScheduledTask();
        this.scheduledFuture = future;
    }

    void cancelScheduledTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }
}