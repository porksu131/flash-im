package com.szr.flashim.gateway.tcp;

import com.szr.flashim.core.netty.DefaultNettyClient;
import com.szr.flashim.core.netty.handler.NettyClientHandler;
import com.szr.flashim.core.netty.processor.NettyClientRequestProcessor;
import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.core.netty.processor.NettyRequestProcessor;
import com.szr.flashim.core.starter.SpringBeanUtils;
import com.szr.flashim.gateway.tcp.config.GatewayNettyClientConfig;
import com.szr.flashim.gateway.tcp.netty.client.manager.DispatchConnectionManager;
import io.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class FlashImGatewayNettyClient extends DefaultNettyClient implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlashImGatewayNettyClient.class);
    private final DispatchConnectionManager dispatchConnectionManager;
    private final String remoteNettyServiceName;

    public FlashImGatewayNettyClient(
            GatewayNettyClientConfig nettyClientConfig,
            DispatchConnectionManager dispatchConnectionManager) {
        super(nettyClientConfig);
        this.nettyClientConfig = nettyClientConfig;
        this.dispatchConnectionManager = dispatchConnectionManager;
        this.remoteNettyServiceName = nettyClientConfig.getServerServiceName();
    }

    @Override
    public void run(String... args) throws Exception {
        start();

        dispatchConnectionManager.bindNettyClient(this);
        // 订阅服务变更
        dispatchConnectionManager.subscribe(remoteNettyServiceName);

        // 初始连接所有服务
        dispatchConnectionManager.connectToAllServers(remoteNettyServiceName);

        //  初始化业务处理器
        initBizProcessor();
    }

    public void start() {
        super.start();
    }

    public void shutdown() {
        super.shutdown();
    }

    public void configChannelHandler(ChannelPipeline pipeline) {
        pipeline.addLast(defaultEventExecutorGroup, new NettyClientHandler(this));
    }

    public DispatchConnectionManager getDispatchConnectionManager() {
        return dispatchConnectionManager;
    }

    public void initBizProcessor() {
        ExecutorService bizProcessorExecutor = (ExecutorService) SpringBeanUtils.getBean("bizProcessorExecutor");
        List<NettyRequestProcessor> requestProcessors = SpringBeanUtils.getBeansOfType(NettyRequestProcessor.class);
        requestProcessors.forEach(processor -> {
            if (processor instanceof NettyClientRequestProcessor) {
                this.getMessageProcessManager().registerMessageProcessor(processor, bizProcessorExecutor);
            }
        });

        List<NettyNotifyProcessor> notifyProcessors = SpringBeanUtils.getBeansOfType(NettyNotifyProcessor.class);
        notifyProcessors.forEach(processor -> {
            this.getMessageProcessManager().registerNotifyProcessor(processor, bizProcessorExecutor);
        });
    }

}
