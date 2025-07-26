package com.szr.flashim.dispatch.config;


import com.szr.flashim.core.netty.config.NettyServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flashim.dispatch.netty.server")
public class DispatchNettyServerConfig extends NettyServerConfig {

}
