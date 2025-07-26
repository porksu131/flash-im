package com.szr.flashim.gateway.tcp.netty.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ServiceException;
import com.szr.flashim.core.netty.exception.SendRequestException;
import com.szr.flashim.core.netty.exception.SendTimeoutException;
import com.szr.flashim.core.netty.manager.ChannelWrapper;
import com.szr.flashim.core.netty.manager.MessageProcessManager;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyClient;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyServer;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.BatchMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.general.model.protoc.CommonResponse;
import com.szr.flashim.general.model.protoc.FriendNotify;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DispatchMessageServiceImpl implements DispatchMessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchMessageServiceImpl.class);
    private static final long DEFAULT_TIME_OUT = 10000;
    @Autowired
    private FlashImGatewayNettyServer nettyServer;

    @Autowired
    private FlashImGatewayNettyClient nettyClient;

    // 此处是网关层的客户端收到了分发系统的用户消息转发，下面是执行转发的逻辑
    @Override
    public ImMessage dispatchMessageToUser(ChannelHandlerContext ctx, ImMessage request) {
        try {
            // 解析消息 获取要发送的目标用户id
            Long toUserId = getToUserId(request);

            // 找到网关的nett服务端与用户连接的通道
            ChannelWrapper userChannel = nettyServer.getChannelCacheManager().getUserChannelCache(toUserId);
            if (userChannel != null && userChannel.isOK()) {
                // 网关的nett服务端进行转发消息到用户
                ImMessage response = nettyServer.getMessageProcessManager().sendSync(
                        userChannel.getChannel(),
                        request,
                        DEFAULT_TIME_OUT);
                CommonResponse userClientRes = CommonResponse.parseFrom(response.getBody());
                boolean success = ResponseCode.SUCCESS == userClientRes.getCode();
                LOGGER.info("消息[{}]转发到用户[{}]{}", request.getMsgId(), toUserId, success ? "成功" : "失败:" + userClientRes.getMsg());
                return response;
            }
            return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "用户[" + toUserId + "]不在线！");
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("消息转换异常：{}", e.getMessage());
            return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "消息未成功转发：" + e.getMessage());
        } catch (SendRequestException | SendTimeoutException e) {
            LOGGER.error("消息转发超时：{}", e.getMessage());
            return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "消息转发超时：" + e.getMessage());
        } catch (ServiceException e) {
            LOGGER.error("消息转发异常：{}", e.getMessage());
            return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "消息转发异常：" + e.getMessage());
        }
    }


    // 此处是网关层的服务端收到了用户客户端的转发消息，下面是执行转发到分发系统的逻辑
    @Override
    public ImMessage dispatchMessageToDispatch(ChannelHandlerContext ctx, ImMessage request) {
        MessageProcessManager messageProcessManager = nettyClient.getMessageProcessManager();
        ChannelWrapper channelWrapper = nettyClient.getDispatchConnectionManager().loadBalanceChannel();
        String address = channelWrapper.getChannelAddress();
        Channel channel = channelWrapper.getChannel();
        try {
            if (channel == null || !channel.isActive()) {
                return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "分发系统：[" + address + "]连接异常！");
            }
            // 发送消息到分发系统
            ImMessage response = messageProcessManager.sendSync(channel, request, DEFAULT_TIME_OUT);
            CommonResponse dispatchRes = CommonResponse.parseFrom(response.getBody());
            boolean success = dispatchRes.getCode() == ResponseCode.SUCCESS;
            LOGGER.info("消息[{}]转发到分发系统[{}]{}", request.getMsgId(), address, success ? "成功" : "失败:" + dispatchRes.getMsg());
            return response;

        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("消息转换异常：{}", e.getMessage(), e);
            return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "消息未成功转发：" + e.getMessage());
        } catch (SendRequestException | SendTimeoutException e) {
            LOGGER.error("网关转发消息到分发系统[{}]异常：{}", address, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // 此处是网关层的客户端收到了分发系统消息后，下面是执行推送通知到用户的逻辑
    @Override
    public void dispatchNotifyToUser(ChannelHandlerContext ctx, ImMessage request) {
        try {
            // 解析消息 获取要发送的目标用户id
            FriendNotify friendNotify = FriendNotify.parseFrom(request.getBody());
            Long uid = friendNotify.getUid();
            Long toUserId = friendNotify.getFriendId();
            // 找到网关的nett服务端与用户连接的通道
            ChannelWrapper userChannel = nettyServer.getChannelCacheManager().getUserChannelCache(toUserId);
            if (userChannel == null || !userChannel.isOK()) {
                LOGGER.error("用户[{}]连接异常，推送通知失败！", toUserId);
                return;
            }
            // 发送通知消息到用户
            nettyServer.getMessageProcessManager().invokeOneway(userChannel.getChannel(), request, DEFAULT_TIME_OUT);
            LOGGER.debug("用户[{}]的好友[{}]通知已发送！", uid, toUserId);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("通知消息转换异常：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (SendRequestException | SendTimeoutException | InterruptedException e) {
            LOGGER.error("网关推送通知消息到用户异常：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // 此处是网关层的服务端收到了用户客户端消息后，下面是执行推送通知到分发系统的逻辑
    @Override
    public void dispatchNotifyToDispatch(ChannelHandlerContext ctx, ImMessage request) {
        dispatchNotifyToDispatch(request);
    }

    public void dispatchNotifyToDispatch(ImMessage request) {
        MessageProcessManager messageProcessManager = nettyClient.getMessageProcessManager();
        ChannelWrapper channelWrapper = nettyClient.getDispatchConnectionManager().loadBalanceChannel();
        String address = channelWrapper.getChannelAddress();
        Channel channel = channelWrapper.getChannel();
        try {
            if (channel == null || !channel.isActive()) {
                LOGGER.error("分发系统[{}]连接异常，推送通知失败！", address);
                return;
            }
            // 发送通知消息到分发系统
            messageProcessManager.invokeOneway(channel, request, DEFAULT_TIME_OUT);

        } catch (SendRequestException | SendTimeoutException | InterruptedException e) {
            LOGGER.error("网关推送通知消息到分发系统[{}]异常：{}", address, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Long getToUserId(ImMessage request) throws ServiceException, InvalidProtocolBufferException {
        Long toUserId = null;
        int subBizType = request.getSubBizType();
        if (SubBizType.SINGLE_MSG.equals(subBizType)) {
            ChatMessage chatMessage = ChatMessage.parseFrom(request.getBody());
            toUserId =  chatMessage.getMessageTo();
        } else if (SubBizType.BATCH_MSG.equals(subBizType)) {
            BatchMessage batchMessage = BatchMessage.parseFrom(request.getBody());
            toUserId = batchMessage.getMessageTo();
        } else {
            throw new ServiceException("subBizType:[" + subBizType + "] not support");
        }
        return toUserId;
    }

}
