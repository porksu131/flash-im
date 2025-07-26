package com.szr.flashim.core.netty.manager;

import com.szr.flashim.core.util.ChannelUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelCacheManager.class);
    //<netty服务端地址，netty客户端跟netty服务端的通道>，通用于netty客户端发送消息
    private final ConcurrentMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();
    //<用户id，用户客户端跟网关连接的通道>，用于网关推送消息到用户
    private final ConcurrentMap<Long, ChannelWrapper> userChannelTables = new ConcurrentHashMap<>();
    //<netty分发系统服务端的地址，网关跟服务端连接的通道>，用于网关随机选择一台分发系统服务端
    private final ConcurrentMap<String, ChannelWrapper> relatedDispatchChannels = new ConcurrentHashMap<>();
    //<netty客户端远程地址，用户ID>，用于网关感知到有客户端离线时，移除相应的用户会话
    private final ConcurrentMap<String, Long> addrUserTables = new ConcurrentHashMap<>();
    private final Lock connectedChannelsLock = new ReentrantLock();

    public ChannelCacheManager() {
    }

    public Channel getActiveChannel(final String addr) {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        return null;
    }

    public ChannelWrapper getChannelWrapper(final String addr) {
        return this.channelTables.get(addr);
    }

    public void closeChannel(String addr, Channel channel) {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw == null) {
            return;
        }
        this.closeChannel(cw.getChannel());
    }

    public void closeChannel(Channel channel) {
        int lockTimeOut = 3000;
        try {
            if (this.connectedChannelsLock.tryLock(lockTimeOut, TimeUnit.MILLISECONDS)) {
                try {
                    String addrRemote = ChannelUtil.parseChannelRemoteAddr(channel);
                    ChannelWrapper ChannelWrapper = this.channelTables.get(addrRemote);
                    if (ChannelWrapper != null && ChannelWrapper.isWrapperOf(channel)) {
                        this.channelTables.remove(addrRemote);
                    }
                    LOGGER.info("closeChannel: the channel[addr={}, id={}] was removed from channel table", addrRemote, channel.id());
                    ChannelUtil.closeChannel(channel);
                } catch (Exception e) {
                    LOGGER.error("closeChannel: close the channel[id={}] exception", channel.id(), e);
                } finally {
                    this.connectedChannelsLock.unlock();
                }
            } else {
                LOGGER.warn("closeChannel: try to lock channel table, but timeout, {}ms", lockTimeOut);
            }
        } catch (InterruptedException e) {
            LOGGER.error("closeChannel exception", e);
        }
    }

    public void updateChannelLastResponseTime(final String addr) {
        ChannelWrapper ChannelWrapper = this.channelTables.get(addr);
        if (ChannelWrapper != null && ChannelWrapper.isOK()) {
            ChannelWrapper.updateLastResponseTime();
        }
    }

    public void saveChannel(String remoteAddress, Channel channel) {
        this.channelTables.put(remoteAddress, new ChannelWrapper(remoteAddress, channel));
    }

    public void removeChannel(String remoteAddress, Channel channel) {
        ChannelWrapper ChannelWrapper = this.channelTables.get(remoteAddress);
        if (ChannelWrapper != null && ChannelWrapper.isWrapperOf(channel)) {
            this.channelTables.remove(remoteAddress);
        }
    }

    public void clear() {
        this.channelTables.clear();
        this.userChannelTables.clear();
    }

    public void removeUserChannelCache(Long uid) {
        this.userChannelTables.remove(uid);
    }

    public ChannelWrapper getUserChannelCache(Long uid) {
        return this.userChannelTables.get(uid);
    }

    public void saveUserChannelCache(Long uid, ChannelWrapper channelWrapper) {
        this.userChannelTables.put(uid, channelWrapper);
    }

    public void saveAddrUserCache(String addr, Long uid) {
        this.addrUserTables.put(addr, uid);
    }

    public void removeAddrUserCache(String addr) {
        this.addrUserTables.remove(addr);
    }

    public Long getUserFromAddrUserCache(String addr) {
        return this.addrUserTables.get(addr);
    }
}
