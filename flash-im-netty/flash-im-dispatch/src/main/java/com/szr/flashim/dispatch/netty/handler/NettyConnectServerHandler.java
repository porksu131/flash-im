package com.szr.flashim.dispatch.netty.handler;

import com.szr.flashim.core.netty.event.NettyEvent;
import com.szr.flashim.core.netty.event.NettyEventExecutor;
import com.szr.flashim.core.netty.event.NettyEventType;
import com.szr.flashim.core.util.ChannelUtil;
import com.szr.flashim.dispatch.FlashImDispatchNettyServer;
import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyConnectServerHandler extends ChannelDuplexHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyConnectServerHandler.class);
    private final NettyEventExecutor nettyEventExecutor;

    private final FlashImDispatchNettyServer nettyServer;

    public NettyConnectServerHandler(NettyEventExecutor nettyEventExecutor, FlashImDispatchNettyServer nettyServer) {
        this.nettyEventExecutor = nettyEventExecutor;
        this.nettyServer = nettyServer;
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        LOGGER.debug("channelRegistered {}", remoteAddress);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        LOGGER.debug("channelUnregistered, the channel[{}]", remoteAddress);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        LOGGER.info("channelActive, the channel[{}]", remoteAddress);
        super.channelActive(ctx);
        nettyServer.getChannelCacheManager().saveChannel(remoteAddress, ctx.channel());
        if (nettyEventExecutor.getChannelEventListeners() != null) {
            nettyEventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        LOGGER.info("channelInactive, the channel[{}]", remoteAddress);
        super.channelInactive(ctx);
        nettyServer.getChannelCacheManager().removeChannel(remoteAddress, ctx.channel());
        if (nettyEventExecutor.getChannelEventListeners() != null) {
            nettyEventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) {
                final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
                LOGGER.debug("IDLE exception [{}]", remoteAddress);
                if (nettyEventExecutor.getChannelEventListeners() != null) {
                    nettyEventExecutor.putNettyEvent(new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel()));
                }
            }
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        final String remoteAddress = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        LOGGER.warn("exceptionCaught {}", remoteAddress);
        LOGGER.warn("exceptionCaught exception.", cause);

        if (nettyEventExecutor.getChannelEventListeners() != null) {
            nettyEventExecutor.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }

        ChannelUtil.closeChannel(ctx.channel());
    }
}
