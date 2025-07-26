package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;

public interface NettyNotifyProcessor {
    int bizType();
    boolean rejectProcess(ImMessage request);
    void processNotify(ChannelHandlerContext ctx, ImMessage message) throws Exception;
}
