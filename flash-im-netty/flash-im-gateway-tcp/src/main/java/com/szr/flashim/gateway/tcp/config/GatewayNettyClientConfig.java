package com.szr.flashim.gateway.tcp.config;

import com.szr.flashim.core.netty.config.NettyClientConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flashim.gateway.netty.client")
public class GatewayNettyClientConfig extends NettyClientConfig {
    private String serverServiceName = "flash-im-dispatch"; // # 要连接的服务在Nacos中的名称
    private int reconnectMaxAttempts = 5; // 最大重连次数
    private long reconnectInitialDelay = 1000; // 第一次重连的延迟时间 单位：ms
    private long reconnectMaxDelay = 30000;  // 重连延迟时间上限 单位：ms
    private double reconnectMultiplier = 2;  // 每次重连延迟时间的倍数

    public double getReconnectMultiplier() {
        return reconnectMultiplier;
    }

    public void setReconnectMultiplier(double reconnectMultiplier) {
        this.reconnectMultiplier = reconnectMultiplier;
    }

    public long getReconnectMaxDelay() {
        return reconnectMaxDelay;
    }

    public void setReconnectMaxDelay(long reconnectMaxDelay) {
        this.reconnectMaxDelay = reconnectMaxDelay;
    }

    public long getReconnectInitialDelay() {
        return reconnectInitialDelay;
    }

    public void setReconnectInitialDelay(long reconnectInitialDelay) {
        this.reconnectInitialDelay = reconnectInitialDelay;
    }

    public int getReconnectMaxAttempts() {
        return reconnectMaxAttempts;
    }

    public void setReconnectMaxAttempts(int reconnectMaxAttempts) {
        this.reconnectMaxAttempts = reconnectMaxAttempts;
    }

    public String getServerServiceName() {
        return serverServiceName;
    }

    public void setServerServiceName(String serverServiceName) {
        this.serverServiceName = serverServiceName;
    }
}
