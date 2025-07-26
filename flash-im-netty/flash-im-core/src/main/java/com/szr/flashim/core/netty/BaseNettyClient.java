package com.szr.flashim.core.netty;

import com.szr.flashim.core.netty.async.InvokeCallback;
import com.szr.flashim.core.netty.exception.NettyConnectException;
import com.szr.flashim.core.netty.exception.SendRequestException;
import com.szr.flashim.core.netty.exception.SendTimeoutException;
import com.szr.flashim.core.netty.manager.BootstrapManager;
import com.szr.flashim.core.netty.manager.ChannelCacheManager;
import com.szr.flashim.core.netty.manager.ChannelWrapper;
import com.szr.flashim.core.netty.manager.MessageProcessManager;
import com.szr.flashim.core.util.ChannelUtil;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.utils.NetworkUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseNettyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseNettyClient.class);
    protected BootstrapManager bootstrapManager = new BootstrapManager();
    protected ChannelCacheManager channelCacheManager = new ChannelCacheManager();
    protected MessageProcessManager messageProcessManager = new MessageProcessManager();

    protected abstract Bootstrap initBootstrap();

    public ImMessage sendMessageSync(String addr, final ImMessage request, long timeoutMillis)
            throws NettyConnectException, SendRequestException, SendTimeoutException {
        long beginStartTime = System.currentTimeMillis();
        final Channel channel = getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            long left = timeoutMillis;
            try {
                long costTime = System.currentTimeMillis() - beginStartTime;
                left = left - costTime;
                if (left <= 0) {
                    throw new SendTimeoutException("invokeSync call the addr[" + addr + "] timeout");
                }
                ImMessage response = messageProcessManager.sendSync(channel, request, left);
                channelCacheManager.updateChannelLastResponseTime(addr);
                return response;
            } catch (SendRequestException e) {
                LOGGER.warn("invokeSync: send request exception, so close the channel[addr={}, id={}]", addr, channel.id());
                throw e;
            } catch (SendTimeoutException e) {
                LOGGER.warn("invokeSync: wait response timeout exception, the channel[addr={}, id={}]", addr, channel.id());
                throw e;
            }
        } else {
            channelCacheManager.closeChannel(addr, channel);
            throw new NettyConnectException(addr);
        }
    }

    public void sendMessageAsync(String addr, final ImMessage request, long timeoutMillis, InvokeCallback invokeCallback)
            throws NettyConnectException, SendTimeoutException {
        long beginStartTime = System.currentTimeMillis();
        final Channel channel = getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            long left = timeoutMillis;
            long costTime = System.currentTimeMillis() - beginStartTime;
            left -= costTime;
            if (left <= 0) {
                throw new SendTimeoutException("invokeAsync call the addr[" + addr + "] timeout");
            }
            messageProcessManager.sendAsync(channel, request, left, invokeCallback);
        } else {
            channelCacheManager.closeChannel(addr, channel);
            throw new NettyConnectException(addr);
        }
    }

    public void sendOneway(String addr, final ImMessage request, long timeoutMillis)
            throws SendRequestException, SendTimeoutException, InterruptedException {
        final Channel channel = getAndCreateChannel(addr);
        messageProcessManager.invokeOneway(channel, request, timeoutMillis);
    }

    public Channel getAndCreateChannel(String addr) {
        ChannelWrapper channelWrapper = channelCacheManager.getChannelWrapper(addr);
        if (channelWrapper != null && channelWrapper.isOK()) {
            return channelWrapper.getChannel();
        }
        Channel newChannel = doConnect(addr);
        channelCacheManager.saveChannel(addr, newChannel);
        return newChannel;
    }

    private Channel doConnect(String addr) {
        try {
            String[] hostAndPort = NetworkUtils.getHostAndPort(addr);
            String host = hostAndPort[0];
            int port = Integer.parseInt(hostAndPort[1]);
            ChannelFuture channelFuture = fetchBootstrap(addr).connect(host, port).sync();
            if (channelFuture.isSuccess()) {
                String localAddr = ChannelUtil.parseChannelLoaclAddr(channelFuture.channel());
                LOGGER.info("local {} connect to server {} success", localAddr, addr);
                return channelFuture.channel();
            } else {
                throw new RuntimeException("connect to server " + addr + " fail");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("connect to server " + addr + " error: " + e.getMessage(), e);
        }
    }

    private Bootstrap fetchBootstrap(String addr) {
        Bootstrap bootstrap = bootstrapManager.getBootstrap(addr);
        if (bootstrap != null) {
            return bootstrap;

        }
        bootstrap = initBootstrap();
        bootstrapManager.addBootstrap(addr, bootstrap);
        return bootstrap;
    }

    public Channel connect(String addr) {
        Channel newChannel = doConnect(addr);
        channelCacheManager.saveChannel(addr, newChannel);
        return newChannel;
    }

    public boolean reconnect(String addr) {
        ChannelWrapper channelWrapper = channelCacheManager.getChannelWrapper(addr);
        if (channelWrapper != null && !channelWrapper.isOK()) {
            Channel oldChannel = channelWrapper.getChannel();
            Channel newChannel = doConnect(addr);
            oldChannel.close();
            channelWrapper.setChannel(newChannel);
            return true;
        }
        return false;
    }

    public BootstrapManager getBootstrapManager() {
        return bootstrapManager;
    }

    public MessageProcessManager getMessageProcessManager() {
        return messageProcessManager;
    }

    public ChannelCacheManager getChannelCacheManager() {
        return channelCacheManager;
    }

}
