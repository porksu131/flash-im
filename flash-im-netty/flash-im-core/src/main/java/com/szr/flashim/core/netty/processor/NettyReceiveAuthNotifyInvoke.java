package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;

public interface NettyReceiveAuthNotifyInvoke {
    void onReceiveNotify(ChannelHandlerContext ctx, ImMessage imMessage) throws Exception;
}
