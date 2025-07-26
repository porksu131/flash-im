package com.szr.flashim.core.netty;

import com.szr.flashim.core.netty.manager.ChannelCacheManager;
import com.szr.flashim.core.netty.manager.MessageProcessManager;

public abstract class BaseNettyServer {
    protected ChannelCacheManager channelCacheManager = new ChannelCacheManager();
    protected MessageProcessManager messageProcessManager = new MessageProcessManager();

    public MessageProcessManager getMessageProcessManager() {
        return messageProcessManager;
    }

    public ChannelCacheManager getChannelCacheManager() {
        return channelCacheManager;
    }
}
