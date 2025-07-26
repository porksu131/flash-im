package com.szr.flashim.gateway.tcp;

import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;


@Component
public class ApplicationCloseEventListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationCloseEventListener.class);
    private final FlashImGatewayNettyClient nettyClient;
    private final FlashImGatewayNettyServer nettyServer;

    public ApplicationCloseEventListener(
            FlashImGatewayNettyClient nettyClient,
            FlashImGatewayNettyServer nettyServer) {
        this.nettyClient = nettyClient;
        this.nettyServer = nettyServer;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("ApplicationCloseEvent");
        nettyClient.shutdown();
        nettyServer.shutdown();
        log.info("nettyClient and nettyServer had shutdown");
    }

}
