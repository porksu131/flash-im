package com.szr.flashim.gateway.tcp;

import com.szr.flashim.core.netty.BaseNettyServer;
import com.szr.flashim.core.netty.event.ChannelEventListener;
import com.szr.flashim.core.netty.event.NettyEventExecutor;
import com.szr.flashim.core.netty.handler.ImMessageDecoder;
import com.szr.flashim.core.netty.handler.ImMessageEncoder;
import com.szr.flashim.core.netty.processor.NettyRequestProcessor;
import com.szr.flashim.core.netty.processor.NettyServerRequestProcessor;
import com.szr.flashim.core.netty.thread.ThreadFactoryImpl;
import com.szr.flashim.core.starter.SpringBeanUtils;
import com.szr.flashim.gateway.tcp.config.GatewayNettyServerConfig;
import com.szr.flashim.gateway.tcp.netty.server.handler.NettyConnectServerHandler;
import com.szr.flashim.gateway.tcp.netty.server.handler.NettyServerBizHandler;
import com.szr.flashim.general.utils.NetworkUtils;
import com.szr.flashim.nacos.starter.NacosDiscovery;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class FlashImGatewayNettyServer extends BaseNettyServer implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlashImGatewayNettyServer.class);

    private EventLoopGroup eventLoopGroupSelector;
    private EventLoopGroup eventLoopGroupBoss;

    @Value("${spring.application.name:flash-im-gateway-tcp}")
    private String serviceName;

    @Autowired
    private GatewayNettyServerConfig nettyConfig;

    @Autowired
    private NettyEventExecutor nettyEventExecutor;

    @Autowired
    private EventExecutorGroup defaultEventExecutorGroup;

    @Autowired
    private NacosDiscovery nacosDiscovery;


    public FlashImGatewayNettyServer(ExecutorService callBackExecutor) {
        this.getMessageProcessManager().setCallbackExecutor(callBackExecutor);
    }


    @Override
    public void run(String... args) throws Exception {
        // 启动netty服务
        nettyServerStart();
        nettyEventExecutor.start();

        // 服务注册 nacos
        String hostName = InetAddress.getLocalHost().getHostName();
        nacosDiscovery.registerInstance(serviceName, hostName, nettyConfig.getListenPort());

        // 初始化业务处理器
        initBizProcessor();

        // 初始化netty事件监听器
        initChannelEventListener();
    }


    public void nettyServerStart() {
        try {
            ServerBootstrap serverBootstrap = initServerBootstrap();
            ChannelFuture sync = serverBootstrap.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            LOGGER.info("netty server started, listening {}:{}", addr.getAddress().getHostAddress(), nettyConfig.getListenPort());
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to bind to %s:%d", nettyConfig.getBindAddress(), nettyConfig.getListenPort()), e);
        }
    }


    public void shutdown() {
        try {
            Thread.sleep(Duration.ofSeconds(nettyConfig.getShutdownWaitTimeSeconds()).toMillis());

            this.getMessageProcessManager().clear();

            if (this.eventLoopGroupBoss != null) {
                this.eventLoopGroupBoss.shutdownGracefully();
            }

            if (this.eventLoopGroupSelector != null) {
                this.eventLoopGroupSelector.shutdownGracefully();
            }

            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }

        } catch (Exception e) {
            LOGGER.error("NettyRemotingServer shutdown exception, ", e);
        }
    }

    private EventLoopGroup buildEventLoopGroupSelector() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(nettyConfig.getServerWorkerThreads(), new ThreadFactoryImpl("NettyServerEPOLLSelector_"));
        } else {
            return new NioEventLoopGroup(nettyConfig.getServerWorkerThreads(), new ThreadFactoryImpl("NettyServerNIOSelector_"));
        }
    }

    private EventLoopGroup buildEventLoopGroupBoss() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(1, new ThreadFactoryImpl("NettyEPOLLBoss_"));
        } else {
            return new NioEventLoopGroup(1, new ThreadFactoryImpl("NettyNIOBoss_"));
        }
    }

    private boolean useEpoll() {
        return NetworkUtils.isLinuxPlatform() && Epoll.isAvailable();
    }

    protected ServerBootstrap initServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        this.eventLoopGroupBoss = buildEventLoopGroupBoss();
        this.eventLoopGroupSelector = buildEventLoopGroupSelector();
        NettyServerBizHandler nettyServerBizHandler = new NettyServerBizHandler(this);
        NettyConnectServerHandler nettyConnectServerHandler = new NettyConnectServerHandler(nettyEventExecutor);
        serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(nettyConfig.getBindAddress(),
                        nettyConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) {
                        channel.pipeline()
                                .addLast(nettyConfig.isServerHandlerExecutorEnable() ? defaultEventExecutorGroup : null
                                        , new ImMessageDecoder()  // 解码
                                        , new ImMessageEncoder()  // 编码
                                        , new IdleStateHandler(0, 0, nettyConfig.getServerChannelMaxIdleTimeSeconds()) // 心跳
                                        , nettyConnectServerHandler // 连接管理
                                        , nettyServerBizHandler // 业务处理
                                );
                    }
                });


        if (nettyConfig.isServerPooledByteBufAllocatorEnable()) {
            serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        return serverBootstrap;
    }

    private void initBizProcessor() {
        ExecutorService bizProcessorExecutor = (ExecutorService) SpringBeanUtils.getBean("bizProcessorExecutor");
        List<NettyRequestProcessor> requestProcessors = SpringBeanUtils.getBeansOfType(NettyRequestProcessor.class);
        requestProcessors.forEach(processor -> {
            if (processor instanceof NettyServerRequestProcessor) {
                this.getMessageProcessManager().registerMessageProcessor(processor, bizProcessorExecutor);
            }
        });
    }

    private void initChannelEventListener() {
        List<ChannelEventListener> channelEventListeners = SpringBeanUtils.getBeansOfType(ChannelEventListener.class);
        channelEventListeners.forEach(listener -> {
            this.nettyEventExecutor.registerListener(listener);
        });
    }
}
