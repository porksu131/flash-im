package com.szr.flashim.gateway.tcp.netty.server.processor;

import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.MsgType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.netty.processor.NettyServerRequestProcessor;
import com.szr.flashim.gateway.tcp.netty.service.DispatchMessageService;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingleChatDispatchProcessor implements NettyServerRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleChatDispatchProcessor.class);

    @Autowired
    private DispatchMessageService dispatchMessageService;

    @Override
    public int bizType() {
        return BizType.SINGLE_CHAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(MsgType.REQUEST.equals(request.getMsgType()) && BizType.SINGLE_CHAT.equals(request.getBizType()));
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) {
        return dispatchMessageService.dispatchMessageToDispatch(ctx, request);
    }
}
