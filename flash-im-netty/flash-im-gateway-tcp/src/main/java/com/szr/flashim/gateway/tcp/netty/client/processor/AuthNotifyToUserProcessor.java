package com.szr.flashim.gateway.tcp.netty.client.processor;

import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.gateway.tcp.netty.service.DispatchMessageService;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthNotifyToUserProcessor implements NettyNotifyProcessor {
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthNotifyToUserProcessor.class);

    @Autowired
    private DispatchMessageService dispatchMessageService;

    @Override
    public int bizType() {
        return BizType.AUTH.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(SubBizType.LOGIN.equals(request.getSubBizType())
                || SubBizType.LOGOUT.equals(request.getSubBizType()));
    }

    @Override
    public void processNotify(ChannelHandlerContext ctx, ImMessage message) throws Exception {
        dispatchMessageService.dispatchNotifyToUser(ctx, message);
    }
}
