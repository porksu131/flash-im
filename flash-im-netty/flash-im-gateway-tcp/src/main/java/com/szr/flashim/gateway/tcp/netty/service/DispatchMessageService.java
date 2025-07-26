package com.szr.flashim.gateway.tcp.netty.service;

import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;

public interface DispatchMessageService {
    ImMessage dispatchMessageToUser(ChannelHandlerContext ctx, ImMessage request);

    ImMessage dispatchMessageToDispatch(ChannelHandlerContext ctx, ImMessage request);

    void dispatchNotifyToUser(ChannelHandlerContext ctx, ImMessage request);

    void dispatchNotifyToDispatch(ChannelHandlerContext ctx, ImMessage request);
}
