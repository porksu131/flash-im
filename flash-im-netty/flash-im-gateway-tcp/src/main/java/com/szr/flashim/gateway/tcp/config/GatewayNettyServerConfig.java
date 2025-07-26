package com.szr.flashim.gateway.tcp.config;


import com.szr.flashim.core.netty.config.NettyServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flashim.gateway.netty.server")
public class GatewayNettyServerConfig extends NettyServerConfig {

}
