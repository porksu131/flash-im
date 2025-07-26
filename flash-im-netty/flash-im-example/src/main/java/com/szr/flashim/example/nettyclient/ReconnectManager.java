package com.szr.flashim.example.nettyclient;

import com.szr.flashim.example.gui.MainFrame;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReconnectManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MainFrame client;
    private volatile boolean isReconnecting = false;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 10;
    private static final long INITIAL_DELAY = 1000;
    private static final long MAX_DELAY = 60000;

    public ReconnectManager(MainFrame client) {
        this.client = client;
    }

    public void scheduleReconnect() {
        if (isReconnecting || attemptCount >= MAX_ATTEMPTS) return;

        isReconnecting = true;
        attemptCount++;

        // 指数退避算法
        long delay = Math.min(INITIAL_DELAY * (long) Math.pow(2, attemptCount - 1), MAX_DELAY);

        scheduler.schedule(() -> {
            try {
                client.connectServer(); // 触发重连
            } finally {
                isReconnecting = false;
            }
        }, delay, TimeUnit.MILLISECONDS);

        // 更新UI状态
        EventQueue.invokeLater(() -> client.updateConnectStatus(delay + "ms后进行重连......"));
    }

    public void reset() {
        attemptCount = 0;
        isReconnecting = false;
    }

    public void sendHeartbeat() {
        client.sendHeartBeat();
        //client.refreshFriendOnlineStatus();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}