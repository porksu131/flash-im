package com.szr.flashim.gateway.tcp.netty.server.handler;


import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class NettyServerBizHandler extends SimpleChannelInboundHandler<ImMessage> {
    private final FlashImGatewayNettyServer nettyServer;

    public NettyServerBizHandler(FlashImGatewayNettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ImMessage msg) {
        this.nettyServer.getMessageProcessManager().processMessageReceived(ctx, msg);
    }
}
