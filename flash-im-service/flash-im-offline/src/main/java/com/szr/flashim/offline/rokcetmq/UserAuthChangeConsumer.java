package com.szr.flashim.offline.rokcetmq;

import com.szr.flashim.general.model.protoc.AuthNotify;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import com.szr.flashim.offline.service.OfflineMessageProcessor;
import com.szr.flashim.offline.service.OfflineMessageService;
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
        consumerGroup = "${rocketmq.consumers.notify-group}",
        messageModel = MessageModel.CLUSTERING)
public class UserAuthChangeConsumer implements RocketMQListener<RocketMqMessage> {
    public static Logger LOGGER = LoggerFactory.getLogger(UserAuthChangeConsumer.class);

    @Autowired
    private OfflineMessageService offlineMessageService;

    @Override
    public void onMessage(RocketMqMessage rocketMqMessage) {
        try {
            if (MqEventType.USER_ONLINE_CHANGE_NOTIFY.equals(rocketMqMessage.getMqEventType())) {
                AuthNotify authNotify = AuthNotify.parseFrom(rocketMqMessage.getContent());
                LOGGER.info("收到用户[{}]上线的消息，推送用户的批量离线消息", authNotify.getUid());
                offlineMessageService.processUserOfflineMessages(authNotify.getUid());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
