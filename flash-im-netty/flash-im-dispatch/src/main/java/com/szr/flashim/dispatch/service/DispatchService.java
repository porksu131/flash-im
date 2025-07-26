package com.szr.flashim.dispatch.service;

import com.szr.flashim.mq.starter.RocketMqMessage;

public interface DispatchService {
    void dispatchMessage(RocketMqMessage message);
    void dispatchNotify(RocketMqMessage message);
}
