package com.szr.flashim.dispatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ApplicationCloseEventListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    private FlashImDispatchNettyServer nettyServer;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        nettyServer.shutdown();
    }

}
