package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuthNotifyProcessor implements NettyNotifyProcessor {
    private static Logger LOGGER = LoggerFactory.getLogger(DefaultAuthNotifyProcessor.class);

    private final NettyReceiveAuthNotifyInvoke nettyReceiveAuthNotifyInvoke;

    public DefaultAuthNotifyProcessor(NettyReceiveAuthNotifyInvoke nettyReceiveAuthNotifyInvoke) {
        this.nettyReceiveAuthNotifyInvoke = nettyReceiveAuthNotifyInvoke;
    }

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
        if (nettyReceiveAuthNotifyInvoke != null) {
            nettyReceiveAuthNotifyInvoke.onReceiveNotify(ctx, message);
        }
    }
}
