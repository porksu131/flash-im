package com.szr.flashim.example.nettyclient;

import com.szr.flashim.core.netty.event.ChannelEventListener;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 家政服务监听器，当发生如下事件时，进行异步的额外处理
 */

public class HousekeepingChannelListener implements ChannelEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingChannelListener.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ReconnectManager reconnectManager;

    public HousekeepingChannelListener(ReconnectManager reconnectManager) {
        this.reconnectManager = reconnectManager;
    }

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelClose(String userAddr, Channel userChannel) {
        // 感知到失去连接，进行重连
        reconnectManager.scheduleReconnect();
    }


    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        // 读写空闲时，发送心跳
        reconnectManager.sendHeartbeat();
    }

    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {
        // 感知到连接可用后
        reconnectManager.reset();
    }

    @Override
    public void onUnregistered(String remoteAddr, Channel channel) {
        // 网络波动时
        scheduler.schedule(reconnectManager::scheduleReconnect, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        reconnectManager.shutdown();
    }
}