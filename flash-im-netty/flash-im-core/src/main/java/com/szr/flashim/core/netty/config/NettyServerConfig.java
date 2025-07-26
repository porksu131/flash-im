package com.szr.flashim.core.netty.config;


public class NettyServerConfig {
    private String bindAddress = "0.0.0.0"; //  绑定地址
    private int listenPort = 8088; // 监听端口
    private int serverBossThreads = 0; // boss线程池数量
    private int serverWorkerThreads = 3; // selector线程池数量

    private boolean serverHandlerExecutorEnable = true; // 是否启用独立线程池执行channelHandler，不启用则默认evenLoop执行
    private int serverHandlerThreads = 8; // channelHandler处理器的工作线程池数量
    private boolean serverPooledByteBufAllocatorEnable = true; // 是否使用池化ByteBufAllocator

    private int serverChannelMaxIdleTimeSeconds = 120; // channel最大（读写）空闲时间，单位：秒，超过该时间会触发相应idle事件
    private int serverAsyncSemaphoreValue = 64; // 异步远程访问的信号量（流量访问限制）
    private int serverCallbackExecutorThreads = 0; // 异步远程访问的回调方法的线程池数量
    private int serverSocketBacklog = 1024; // 服务端socket连接队列大小

    private int shutdownWaitTimeSeconds = 3; // 优雅关闭等待时间

    private boolean useSSL = false; // 是否使用安全传输ssl
    private String tlsServerCertPath = ""; // 服务端证书路径
    private String tlsServerKeyPath = ""; // 服务端私钥路径


    /**
     * getter/setter
     */

    public String getTlsServerCertPath() {
        return tlsServerCertPath;
    }

    public void setTlsServerCertPath(String tlsServerCertPath) {
        this.tlsServerCertPath = tlsServerCertPath;
    }

    public String getTlsServerKeyPath() {
        return tlsServerKeyPath;
    }

    public void setTlsServerKeyPath(String tlsServerKeyPath) {
        this.tlsServerKeyPath = tlsServerKeyPath;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getListenPort() {
        return listenPort;
    }

    public boolean isServerHandlerExecutorEnable() {
        return serverHandlerExecutorEnable;
    }

    public void setServerHandlerExecutorEnable(boolean serverHandlerExecutorEnable) {
        this.serverHandlerExecutorEnable = serverHandlerExecutorEnable;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    public int getServerCallbackExecutorThreads() {
        return serverCallbackExecutorThreads;
    }

    public void setServerCallbackExecutorThreads(int serverCallbackExecutorThreads) {
        this.serverCallbackExecutorThreads = serverCallbackExecutorThreads;
    }

    public int getServerBossThreads() {
        return serverBossThreads;
    }

    public void setServerBossThreads(int serverBossThreads) {
        this.serverBossThreads = serverBossThreads;
    }

    public int getServerHandlerThreads() {
        return serverHandlerThreads;
    }

    public void setServerHandlerThreads(int serverHandlerThreads) {
        this.serverHandlerThreads = serverHandlerThreads;
    }

    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }

    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }

    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }

    public void setServerChannelMaxIdleTimeSeconds(int serverChannelMaxIdleTimeSeconds) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
    }

    public int getServerSocketBacklog() {
        return serverSocketBacklog;
    }

    public void setServerSocketBacklog(int serverSocketBacklog) {
        this.serverSocketBacklog = serverSocketBacklog;
    }

    public boolean isServerPooledByteBufAllocatorEnable() {
        return serverPooledByteBufAllocatorEnable;
    }

    public void setServerPooledByteBufAllocatorEnable(boolean serverPooledByteBufAllocatorEnable) {
        this.serverPooledByteBufAllocatorEnable = serverPooledByteBufAllocatorEnable;
    }

    public int getShutdownWaitTimeSeconds() {
        return shutdownWaitTimeSeconds;
    }

    public void setShutdownWaitTimeSeconds(int shutdownWaitTimeSeconds) {
        this.shutdownWaitTimeSeconds = shutdownWaitTimeSeconds;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
}
