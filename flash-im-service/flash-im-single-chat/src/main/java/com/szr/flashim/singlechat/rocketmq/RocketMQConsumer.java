package com.szr.flashim.singlechat.rocketmq;

import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监听模式，集群模式
 * 消费模式，顺序消费
 */
@Service
@RocketMQMessageListener(topic = MqTopicConstant.SING_CHAT_TOPIC,
        selectorExpression = "chat-event",
        consumerGroup = "single-chat-consumer-group",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.ORDERLY)
public class RocketMQConsumer implements RocketMQListener<RocketMqMessage> {

    @Autowired
    private SingleChatService singleChatService;

    @Override
    public void onMessage(RocketMqMessage message) {
        singleChatService.processMqMessage(message);
    }
}
