package com.szr.flashim.dispatch.rocketmq;

import com.szr.flashim.dispatch.service.DispatchService;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * messageModel=MessageModel.CLUSTERING
 * 监听模式，有消息就会消费
 */
@Service
@RocketMQMessageListener(topic = MqTopicConstant.MSG_DISPATCH_SINGLE_SEND_TOPIC,
        consumerGroup = "${rocketmq.consumers.message-group}",
        messageModel = MessageModel.CLUSTERING)
public class DispatchMessageConsumer implements RocketMQListener<RocketMqMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchMessageConsumer.class);

    @Autowired
    private DispatchService dispatchService;

    @Override
    public void onMessage(RocketMqMessage message) {
        LOGGER.debug("receive message from mq, msgType: {}", message.getMqEventType());
        dispatchService.dispatchMessage(message);
    }
}
