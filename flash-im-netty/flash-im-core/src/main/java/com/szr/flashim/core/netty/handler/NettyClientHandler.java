package com.szr.flashim.core.netty.handler;

import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.netty.BaseNettyClient;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<ImMessage> {
    private final BaseNettyClient nettyClient;;

    public NettyClientHandler(BaseNettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ImMessage msg) throws Exception {
        this.nettyClient.getMessageProcessManager().processMessageReceived(ctx, msg);
    }
}
