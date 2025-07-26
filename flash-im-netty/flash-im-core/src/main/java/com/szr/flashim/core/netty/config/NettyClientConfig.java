package com.szr.flashim.core.netty.config;

public class NettyClientConfig {
    private String bindAddress = "";
    private int bindPort = 0;
    private int clientWorkerThreads = 4;
    //private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private int clientCallbackExecutorThreads = 8;

    private int connectTimeoutMillis = 3000;
    private int clientChannelMaxIdleTimeSeconds = 120;

    private int clientAsyncSemaphoreValue = 65535;

    private long lockTimeoutMillis = 3000;
    private long minCloseTimeoutMillis = 100;

    private long sendTimeoutSeconds = 60;
    private boolean useSSL = false;

    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }

    public void setClientWorkerThreads(int clientWorkerThreads) {
        this.clientWorkerThreads = clientWorkerThreads;
    }

    public int getClientCallbackExecutorThreads() {
        if (clientCallbackExecutorThreads <= 0) {
            clientCallbackExecutorThreads = 4;
        }
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }

    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }

    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }

    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }

    public long getLockTimeoutMillis() {
        return lockTimeoutMillis;
    }

    public void setLockTimeoutMillis(long lockTimeoutMillis) {
        this.lockTimeoutMillis = lockTimeoutMillis;
    }

    public long getMinCloseTimeoutMillis() {
        return minCloseTimeoutMillis;
    }

    public void setMinCloseTimeoutMillis(long minCloseTimeoutMillis) {
        this.minCloseTimeoutMillis = minCloseTimeoutMillis;
    }

    public long getSendTimeoutSeconds() {
        return sendTimeoutSeconds;
    }

    public void setSendTimeoutSeconds(long sendTimeoutSeconds) {
        this.sendTimeoutSeconds = sendTimeoutSeconds;
    }

    public int getBindPort() {
        return bindPort;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
}
