package com.szr.flashim.gateway.tcp.netty.service;

public interface AuthorizeService {
    boolean isAuth(long uid, String token);
}
