package com.szr.flashim.core.netty.manager;

import com.szr.flashim.core.util.ChannelUtil;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChannelWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelWrapper.class);

    private final ReentrantReadWriteLock lock;
    private Channel channel;
    private long lastResponseTime;
    private final String channelAddress;

    public ChannelWrapper(String address, Channel channel) {
        this.lock = new ReentrantReadWriteLock();
        this.channel = channel;
        this.lastResponseTime = System.currentTimeMillis();
        this.channelAddress = address;
    }

    public boolean isOK() {
        return getChannel() != null && getChannel().isActive();
    }

    public boolean isWritable() {
        return getChannel().isWritable();
    }

    public boolean isWrapperOf(Channel channel) {
        return this.channel == channel;
    }

    public Channel getChannel() {
        lock.readLock().lock();
        try {
            return this.channel;
        } finally {
            lock.readLock().unlock();
        }
    }

    public long getLastResponseTime() {
        return this.lastResponseTime;
    }

    public void updateLastResponseTime() {
        this.lastResponseTime = System.currentTimeMillis();
    }

    public String getChannelAddress() {
        return channelAddress;
    }

    public void close() {
        try {
            lock.writeLock().lock();
            if (channel != null) {
                ChannelUtil.closeChannel(channel);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setChannel(Channel newChannel) {
        try {
            lock.writeLock().lock();
            this.channel = newChannel;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
