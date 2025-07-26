package com.szr.flashim.gateway.tcp.netty.client.processor;

import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.netty.processor.NettyClientRequestProcessor;
import com.szr.flashim.gateway.tcp.netty.service.DispatchMessageService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatchToUserProcessor implements NettyClientRequestProcessor {

    @Autowired
    private DispatchMessageService dispatchMessageService;

    @Override
    public int bizType() {
        return BizType.SINGLE_CHAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(SubBizType.SINGLE_MSG.equals(request.getSubBizType())
                || SubBizType.BATCH_MSG.equals(request.getSubBizType()));
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) {
        return dispatchMessageService.dispatchMessageToUser(ctx, request);
    }
}
