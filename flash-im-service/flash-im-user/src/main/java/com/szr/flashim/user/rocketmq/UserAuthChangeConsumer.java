package com.szr.flashim.user.rocketmq;

import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import com.szr.flashim.user.service.UserConnectionService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监听模式，有消息就会消费
 */
@Service
@RocketMQMessageListener(topic = MqTopicConstant.USR_AUTH_CHANGE_TOPIC,
        consumerGroup = "user-consumer-group",
        messageModel = MessageModel.CLUSTERING)
public class UserAuthChangeConsumer implements RocketMQListener<RocketMqMessage> {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthChangeConsumer.class);

    @Autowired
    private UserConnectionService userConnectionService;

    @Override
    public void onMessage(RocketMqMessage message) {
        try {
            userConnectionService.processMqMessage(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}