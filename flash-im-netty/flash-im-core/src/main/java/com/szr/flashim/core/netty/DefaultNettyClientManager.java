package com.szr.flashim.core.netty;

import com.szr.flashim.core.netty.async.InvokeCallback;
import com.szr.flashim.core.netty.async.NettyClientSendCallBack;
import com.szr.flashim.core.netty.config.NettyClientConfig;
import com.szr.flashim.core.netty.event.ChannelEventListener;
import com.szr.flashim.core.netty.processor.NettyClientRequestProcessor;
import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.CommonResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DefaultNettyClientManager {
    private final DefaultNettyClient nettyClient;

    public DefaultNettyClientManager(NettyClientConfig nettyClientConfig,
                                     List<NettyClientRequestProcessor> nettyRequestProcessors,
                                     ExecutorService messageProcessorExecutor,
                                     List<NettyNotifyProcessor> nettyNotifyProcessors,
                                     ExecutorService notifyExecutorService,
                                     List<ChannelEventListener> channelEventListeners) {
        this.nettyClient = new DefaultNettyClient(nettyClientConfig);
        this.nettyClient.registerMessageProcessors(nettyRequestProcessors, messageProcessorExecutor);
        this.nettyClient.registerNotifyProcessors(nettyNotifyProcessors, notifyExecutorService);
        this.nettyClient.nettyEventExecutor.registerListeners(channelEventListeners);
    }

    public void sendMessageAsync(String addr, final ImMessage request, long timeoutMillis, NettyClientSendCallBack sendCallBack) {
        try {
            this.nettyClient.sendMessageAsync(addr, request, timeoutMillis, new InvokeCallback() {
                @Override
                public void operationSucceed(ImMessage response) {
                    CommonResponse commonResponse = null;
                    try {
                        commonResponse = CommonResponse.parseFrom(response.getBody());
                    } catch (Exception e) {
                        commonResponse = CommonResponse.newBuilder().setCode(ResponseCode.SYSTEM_ERROR).setMsg(e.getMessage()).build();
                    }
                    if (sendCallBack != null) {
                        sendCallBack.onSendSuccess(commonResponse);
                    }
                }

                @Override
                public void operationFail(Throwable throwable) {
                    if (sendCallBack != null) {
                        sendCallBack.onSendFail(throwable);
                    }
                }
            });
        } catch (Exception e) {
            if (sendCallBack != null) {
                sendCallBack.onSendFail(e);
            }
        }
    }


    public void sendWithResponse(String addr, final ImMessage request, long timeoutMillis) throws Exception {
        this.nettyClient.sendOneway(addr, request, timeoutMillis);
    }

    public void start() {
        this.nettyClient.start();
    }

    public void shutdown() {
        if (this.nettyClient != null) {
            this.nettyClient.shutdown();
        }
    }
}
