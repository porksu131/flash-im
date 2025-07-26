package com.szr.flashim.core.netty.event;

import com.szr.flashim.core.netty.thread.ServiceThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NettyEventExecutor extends ServiceThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyEventExecutor.class);
    private static final int EVENT_QUEUE = 10000;
    private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<>();
    private final List<ChannelEventListener> channelEventListeners = new ArrayList<>();

    public void putNettyEvent(final NettyEvent event) {
        if (this.eventQueue.size() <= EVENT_QUEUE) {
            this.eventQueue.add(event);
        } else {
            LOGGER.warn("event queue size [{}] over the limit [{}], so drop this event {}",
                    this.eventQueue.size(), EVENT_QUEUE, event.toString());
        }
    }

    @Override
    public void run() {
        LOGGER.info("{} service started", this.getServiceName());
        while (!this.isStopped()) {
            processEvent();
        }
        LOGGER.info("{} service end", this.getServiceName());
    }

    private void processEvent() {
        try {
            if (this.channelEventListeners.isEmpty()) {
                return;
            }
            NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
            if (event == null) {
                return;
            }
            for (ChannelEventListener listener : this.channelEventListeners) {
                switch (event.getType()) {
                    case IDLE:
                        listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                        break;
                    case CLOSE:
                        listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                        break;
                    case CONNECT:
                        listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                        break;
                    case EXCEPTION:
                        listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                        break;
                    case ACTIVE:
                        listener.onChannelActive(event.getRemoteAddr(), event.getChannel());
                        break;
                    case UNREGISTERED:
                        listener.onUnregistered(event.getRemoteAddr(), event.getChannel());
                        break;
                    default:
                        break;

                }
            }
        } catch (Exception e) {
            LOGGER.warn("{}service has exception. ", this.getServiceName(), e);
        }
    }

    public void registerListener(ChannelEventListener listener) {
        this.channelEventListeners.add(listener);
    }

    public void registerListeners(List<ChannelEventListener> listeners) {
        if (listeners != null) {
            this.channelEventListeners.addAll(listeners);
        }
    }

    public List<ChannelEventListener> getChannelEventListeners() {
        return this.channelEventListeners;
    }

    @Override
    public String getServiceName() {
        return NettyEventExecutor.class.getSimpleName();
    }
}
