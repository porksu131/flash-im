package com.szr.flashim.core.netty.manager;

import io.netty.bootstrap.Bootstrap;

import java.util.HashMap;
import java.util.Map;

public class BootstrapManager {
    private final Map<String, Bootstrap> bootstrapMap = new HashMap<>(16);

    public Bootstrap getBootstrap(String addr) {
        return bootstrapMap.get(addr);
    }

    public void addBootstrap(String addr, Bootstrap bootstrap) {
        bootstrapMap.putIfAbsent(addr, bootstrap);
    }

    public void removeBootstrap(String addr) {
        bootstrapMap.remove(addr);
    }

    public void clear() {
        bootstrapMap.clear();
    }

    public void closeAllBootstrap() {
        for (Bootstrap bootstrap : bootstrapMap.values()) {
            bootstrap.config().group().shutdownGracefully();
        }
    }

}
