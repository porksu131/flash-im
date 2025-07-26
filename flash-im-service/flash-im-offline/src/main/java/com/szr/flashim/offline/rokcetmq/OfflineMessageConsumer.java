package com.szr.flashim.offline.rokcetmq;

import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import com.szr.flashim.offline.service.OfflineMessageProcessor;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监听模式，有消息就会消费
 */
@Service
@RocketMQMessageListener(topic = MqTopicConstant.MSG_DISPATCH_FAIL_TOPIC,
        consumerGroup = "${rocketmq.consumers.message-group}",
        messageModel = MessageModel.CLUSTERING)
public class OfflineMessageConsumer implements RocketMQListener<RocketMqMessage> {

    @Autowired
    private OfflineMessageProcessor offlineMessageProcessor;

    @Override
    public void onMessage(RocketMqMessage message) {
        try {
            offlineMessageProcessor.processMqMessage(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
