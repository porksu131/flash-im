package com.szr.flashim.gateway.tcp.netty.server.processor;

import com.flashim.szr.cache.starter.UserSessionManager;
import com.szr.flashim.core.netty.processor.NettyServerRequestProcessor;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.HeartBeat;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class HeartBeatProcessor implements NettyServerRequestProcessor {
    private static Logger LOGGER = LoggerFactory.getLogger(HeartBeatProcessor.class);
    @Autowired
    private ExecutorService bizProcessorExecutor;
    @Autowired
    private UserSessionManager userSessionManager;

    @Override
    public int bizType() {
        return BizType.HEART_BEAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return request.getBizType() != BizType.HEART_BEAT.getCode();
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) throws Exception {
        bizProcessorExecutor.submit(() -> {
            try {
                HeartBeat heartBeat = HeartBeat.parseFrom(request.getBody());
                // 更新用户活跃时间
                userSessionManager.setUserOnline(heartBeat.getUid());
            } catch (Exception e) {
                LOGGER.error("心跳处理异常：{}", e.getMessage(), e);
            }

        });
        return ImMessage.createMessageResponse(request, ResponseCode.SUCCESS, "心跳接收成功");
    }
}
