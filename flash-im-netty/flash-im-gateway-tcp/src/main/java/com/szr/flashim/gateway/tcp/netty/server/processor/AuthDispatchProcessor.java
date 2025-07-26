package com.szr.flashim.gateway.tcp.netty.server.processor;

import com.flashim.szr.cache.starter.CacheConstant;
import com.flashim.szr.cache.starter.UserSessionManager;
import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.core.netty.manager.ChannelWrapper;
import com.szr.flashim.core.netty.processor.NettyServerRequestProcessor;
import com.szr.flashim.core.util.ChannelUtil;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyClient;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyServer;
import com.szr.flashim.gateway.tcp.netty.service.AuthorizeService;
import com.szr.flashim.gateway.tcp.netty.service.DispatchMessageService;
import com.szr.flashim.general.constant.AuthResponseCode;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.NotifyType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.exception.ServiceException;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.AuthMessage;
import com.szr.flashim.general.model.protoc.AuthNotify;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class AuthDispatchProcessor implements NettyServerRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDispatchProcessor.class);

    private final UserSessionManager userSessionManager;
    private final AuthorizeService authorizeService;
    private final FlashImGatewayNettyClient gatewayNettyClient;
    private final FlashImGatewayNettyServer gatewayNettyServer;
    private final DispatchMessageService dispatchMessageService;
    private final SnowflakeIdGenerator idGenerator;
    private final ExecutorService bizProcessorExecutor;

    public AuthDispatchProcessor(UserSessionManager userSessionManager,
                                 AuthorizeService authorizeService,
                                 FlashImGatewayNettyClient gatewayNettyClient,
                                 FlashImGatewayNettyServer gatewayNettyServer,
                                 DispatchMessageService dispatchMessageService,
                                 SnowflakeIdGenerator idGenerator,
                                 ExecutorService bizProcessorExecutor) {
        this.userSessionManager = userSessionManager;
        this.authorizeService = authorizeService;
        this.gatewayNettyClient = gatewayNettyClient;
        this.gatewayNettyServer = gatewayNettyServer;
        this.dispatchMessageService = dispatchMessageService;
        this.idGenerator = idGenerator;
        this.bizProcessorExecutor = bizProcessorExecutor;
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
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) throws InvalidProtocolBufferException {
        if (SubBizType.LOGIN.equals(request.getSubBizType())) {
            return login(ctx, request);
        } else if (SubBizType.LOGOUT.equals(request.getSubBizType())) {
            return logout(ctx, request);
        }
        throw new ServiceException("未知的子业务类型：" + request.getSubBizType());
    }

    // 登录
    public ImMessage login(ChannelHandlerContext ctx, ImMessage request) throws InvalidProtocolBufferException {
        String userAddr = ChannelUtil.parseChannelRemoteAddr(ctx.channel());
        String gatewayAddr = getGatewayClientAddr();
        AuthMessage authMessage = AuthMessage.parseFrom(request.getBody());
        //  验证用户信息
        if (!authorizeService.isAuth(authMessage.getUid(), authMessage.getToken())) {
            LOGGER.debug("消息[{}]已处理，用户[{}]认证失败！", request.getMsgId(), authMessage.getUid());
            return ImMessage.createMessageResponse(request, AuthResponseCode.FAIL, "认证失败！");
        }

        // Local缓存用户会话信息
        gatewayNettyServer.getChannelCacheManager().saveUserChannelCache(authMessage.getUid(), new ChannelWrapper(userAddr, ctx.channel()));
        gatewayNettyServer.getChannelCacheManager().saveAddrUserCache(userAddr, authMessage.getUid());

        // Redis缓存会话信息，后面分发系统会用于消息转发，也就是根据用户id找到与用户连接的网关
        saveRedisSession(authMessage, gatewayAddr);

        // 登录，更新redis用户的状态为在线
        userSessionManager.setUserOnline(authMessage.getUid());

        // 推送用户上线消息
        dispatchNotifyToDispatch(ctx, NotifyType.ON_LINE, authMessage);

        LOGGER.debug("消息[{}]已处理，用户[{}]认证成功！", request.getMsgId(), authMessage.getUid());
        return ImMessage.createMessageResponse(request, AuthResponseCode.SUCCESS, "认证成功！");
    }

    // 登出
    public ImMessage logout(ChannelHandlerContext ctx, ImMessage request) throws InvalidProtocolBufferException {
        AuthMessage authMessage = AuthMessage.parseFrom(request.getBody());
        if (!authorizeService.isAuth(authMessage.getUid(), authMessage.getToken())) {
            LOGGER.debug("消息[{}]已处理，用户[{}]验证token失败！", request.getMsgId(), authMessage.getUid());
            return ImMessage.createMessageResponse(request, AuthResponseCode.FAIL, "登出失败！");
        }
        // 登出，移除Local中的用户会话信息
        gatewayNettyServer.getChannelCacheManager().removeUserChannelCache(authMessage.getUid());
        // 登出，移除redis中的用户会话信息
        userSessionManager.removeUserRedisSession(authMessage.getUid());
        // 登出，更新redis用户的状态为离线
        userSessionManager.setUserOffline(authMessage.getUid());

        // 推送用户离线消息
        dispatchNotifyToDispatch(ctx, NotifyType.OFF_LINE, authMessage);

        return ImMessage.createMessageResponse(request, AuthResponseCode.SUCCESS, "登出成功！");
    }


    private void dispatchNotifyToDispatch(ChannelHandlerContext ctx, NotifyType notifyType, AuthMessage authMessage) {
        bizProcessorExecutor.submit(() -> {
            AuthNotify authNotify = AuthNotify.newBuilder()
                    .setUid(authMessage.getUid())
                    .setOperationType(notifyType.getCode())
                    .setOperationTime(System.currentTimeMillis())
                    .build();
            ImMessage onlineNotify = ImMessage.createOnlineNotify(idGenerator.nextId(), authNotify);
            dispatchMessageService.dispatchNotifyToDispatch(ctx, onlineNotify);
        });
    }

    private void saveRedisSession(AuthMessage authMessage, String gatewayAddress) {
        Map<String, Object> userAuthMap = new HashMap<>();
        userAuthMap.put("uid", authMessage.getUid());
        //userAuthMap.put("token", authMessage.getToken()); // token有点大 没必要存，
        userAuthMap.put("requestTime", authMessage.getCreateTime());
        userAuthMap.put("sessionTime", System.currentTimeMillis());
        userAuthMap.put("gatewayAddress", gatewayAddress);
        userSessionManager.addUserRedisSession(authMessage.getUid(), userAuthMap);
    }

    private String getGatewayClientAddr() {
        ChannelWrapper channelWrapper = this.gatewayNettyClient.getDispatchConnectionManager().loadBalanceChannel();
        Channel channel = channelWrapper.getChannel();
        return ChannelUtil.parseChannelLoaclAddr(channel);
    }

}
