package com.szr.flashim.gateway.tcp.netty.server.listener;

import com.flashim.szr.cache.starter.UserSessionManager;
import com.szr.flashim.core.netty.event.ChannelEventListener;
import com.szr.flashim.core.netty.manager.ChannelCacheManager;
import com.szr.flashim.gateway.tcp.FlashImGatewayNettyServer;
import com.szr.flashim.gateway.tcp.netty.service.DispatchMessageServiceImpl;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.enumeration.NotifyType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.AuthNotify;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 家政服务监听器，当发生如下事件时，进行异步的额外处理
 */
@Component
public class HousekeepingChannelListener implements ChannelEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingChannelListener.class);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
    @Autowired
    private FlashImGatewayNettyServer gatewayNettyServer;
    @Autowired
    private DispatchMessageServiceImpl dispatchMessageService;
    @Autowired
    private ExecutorService bizProcessorExecutor;
    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @Autowired
    private UserSessionManager userSessionManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {
        LOGGER.info("{} -- channel connected", remoteAddr);
    }

    @Override
    public void onChannelClose(String userAddr, Channel userChannel) {
        ChannelCacheManager channelCacheManager = gatewayNettyServer.getChannelCacheManager();
        Long uid = channelCacheManager.getUserFromAddrUserCache(userAddr);
        if (uid == null) {
            return;
        }

        // 移除Redis的会话信息
        userSessionManager.removeUserRedisSession(uid);

        // 移除Local的会话信息
        channelCacheManager.removeUserChannelCache(uid);
        channelCacheManager.removeAddrUserCache(userAddr);

        // 延迟5秒检测是否重连
        SCHEDULER.schedule(() -> {
            // 为重连则更新用户状态
            if (channelCacheManager.getUserChannelCache(uid) == null) {
                // 更新redis用过户的在线状态为离线
                userSessionManager.setUserOffline(uid);
                // 推送通知到分发系统
                pushNotifyToDispatch(uid, false);
            }
        }, 5, TimeUnit.SECONDS);

        LOGGER.info("感知到远程用户地址[{}]连接关闭，移除用户[{}]的会话信息", userAddr, uid);
    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {
        LOGGER.debug("{} -- channel exception", remoteAddr);
    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {
        LOGGER.debug("{} -- channel idle", remoteAddr);
    }

    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {
//        ChannelCacheManager channelCacheManager = gatewayNettyServer.getChannelCacheManager();
//        Long uid = channelCacheManager.getUserFromAddrUserCache(remoteAddr);
//        // 更新redis用过户的在线状态为在线
//        userSessionManager.setOnline(uid);
//        // 推送通知到分发系统
//        pushNotifyToDispatch(uid, true);
        // 在认证通过后才算连上，所以上线通知不在此处做
        LOGGER.debug("{} -- channel active", remoteAddr);
    }

    @Override
    public void onUnregistered(String remoteAddr, Channel channel) {
        LOGGER.debug("{} -- channel unregistered", remoteAddr);
    }

    private void pushNotifyToDispatch(Long uid, boolean isOnline) {
        NotifyType notifyType = isOnline ? NotifyType.ON_LINE : NotifyType.OFF_LINE;
        bizProcessorExecutor.submit(() -> {
            AuthNotify authNotify = AuthNotify.newBuilder()
                    .setUid(uid)
                    .setOperationType(notifyType.getCode())
                    .setOperationTime(System.currentTimeMillis())
                    .build();
            ImMessage onlineNotify = ImMessage.createOnlineNotify(idGenerator.nextId(), authNotify);
            dispatchMessageService.dispatchNotifyToDispatch(onlineNotify);
        });
    }

}