package com.szr.flashim.core.netty;

import com.szr.flashim.core.netty.handler.ImMessageDecoder;
import com.szr.flashim.core.netty.handler.ImMessageEncoder;
import com.szr.flashim.core.netty.config.NettyClientConfig;
import com.szr.flashim.core.netty.event.NettyEventExecutor;
import com.szr.flashim.core.netty.handler.NettyClientHandler;
import com.szr.flashim.core.netty.handler.NettyConnectClientHandler;
import com.szr.flashim.core.netty.processor.NettyClientRequestProcessor;
import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.core.netty.thread.ThreadFactoryImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultNettyClient extends BaseNettyClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultNettyClient.class);
    protected NettyClientConfig nettyClientConfig;
    protected EventLoopGroup eventLoopGroupWorker;
    protected EventExecutorGroup defaultEventExecutorGroup;
    protected NettyEventExecutor nettyEventExecutor;
    protected ExecutorService publicExecutor;
    protected ExecutorService callBackExecutor;

    public DefaultNettyClient(final NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;
        this.callBackExecutor = Executors.newFixedThreadPool(
                nettyClientConfig.getClientCallbackExecutorThreads(),
                new ThreadFactoryImpl("NettyClientCallBackExecutor_"));
        this.publicExecutor = Executors.newFixedThreadPool(
                nettyClientConfig.getClientCallbackExecutorThreads(),
                new ThreadFactoryImpl("NettyClientPublicExecutor_"));

        this.eventLoopGroupWorker = new NioEventLoopGroup(
                1,
                new ThreadFactoryImpl("NettyClientSelector_"));
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyClientConfig.getClientWorkerThreads(),
                new ThreadFactoryImpl("NettyClientWorkerThread_"));
        this.nettyEventExecutor = new NettyEventExecutor();

        loadSslContext();
    }

    public void loadSslContext() {
        if (this.nettyClientConfig.isUseSSL()) {
            //loadSslContext(true);
        }
    }

    public void start() {
        if (this.nettyEventExecutor != null) {
            nettyEventExecutor.start();
        }
    }

    public void shutdown() {
        try {
            this.channelCacheManager.clear();
            this.bootstrapManager.closeAllBootstrap();
            this.eventLoopGroupWorker.shutdownGracefully();

            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            LOGGER.error("NettyClient shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                LOGGER.error("NettyClient shutdown exception, ", e);
            }
        }
    }

    @Override
    public Bootstrap initBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroupWorker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyClientConfig.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(
                                defaultEventExecutorGroup,
                                new ImMessageEncoder(),
                                new ImMessageDecoder(),
                                new IdleStateHandler(0, 0, nettyClientConfig.getClientChannelMaxIdleTimeSeconds())                        );

                        configChannelHandler(pipeline);
                    }
                });
        if (nettyClientConfig.getBindPort() != 0 && nettyClientConfig.getBindAddress() != null) {
            bootstrap.localAddress(nettyClientConfig.getBindAddress(), nettyClientConfig.getBindPort());
        }
        return bootstrap;
    }

    public void configChannelHandler(ChannelPipeline pipeline) {
        pipeline.addLast(defaultEventExecutorGroup, new NettyConnectClientHandler(nettyEventExecutor));
        pipeline.addLast(defaultEventExecutorGroup, new NettyClientHandler(this));
    }

    public void registerMessageProcessors(List<NettyClientRequestProcessor> processors, ExecutorService executor) {
        this.messageProcessManager.registerMessageProcessors(processors, executor == null ? this.publicExecutor : executor);
    }


    public void registerNotifyProcessors(List<NettyNotifyProcessor> processors, ExecutorService executor) {
        this.messageProcessManager.registerNotifyProcessors(processors, executor == null ? this.publicExecutor : executor);
    }
}
