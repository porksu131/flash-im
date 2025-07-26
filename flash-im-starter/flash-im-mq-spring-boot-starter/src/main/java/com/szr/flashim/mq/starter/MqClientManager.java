package com.szr.flashim.mq.starter;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

import java.time.LocalDateTime;
import java.util.UUID;


public class MqClientManager {
    private final RocketMQProducer rocketMQProducer;

    public MqClientManager(RocketMQProducer rocketMQProducer) {
        this.rocketMQProducer = rocketMQProducer;
    }

    public void sendAsyncMessage(String topic, MqEventType mqEventType, byte[] content) {
        sendAsyncMessage(topic, mqEventType, content, null);
    }

    public void sendAsyncMessage(String topic, MqEventType mqEventType, byte[] content, SendCallback sendCallback) {
        rocketMQProducer.sendAsyncMessage(topic, buildDefault(mqEventType, content), sendCallback);
    }

    public SendResult sendSyncMessage(String topic, MqEventType mqEventType, byte[] content) {
        return rocketMQProducer.sendSyncMessage(topic, buildDefault(mqEventType, content));
    }

    public void sendAsyncOrderlyMessage(String topic, MqEventType mqEventType, byte[] content, String hashKey, SendCallback sendCallback) {
        rocketMQProducer.sendAsyncOrderlyMessage(topic, buildDefault(mqEventType, content), hashKey, sendCallback);
    }

    public SendResult sendSyncOrderlyMessage(String topic, MqEventType mqEventType, byte[] content, String hashKey) {
        return rocketMQProducer.sendSyncOrderlyMessage(topic, buildDefault(mqEventType, content), hashKey);
    }

    private RocketMqMessage buildDefault(MqEventType mqEventType, byte[] content) {
        RocketMqMessage rocketMqMessage = new RocketMqMessage();
        rocketMqMessage.setId(UUID.randomUUID().toString());
        rocketMqMessage.setTimestamp(LocalDateTime.now());
        rocketMqMessage.setMqEventType(mqEventType.getCode());
        rocketMqMessage.setContent(content);
        return rocketMqMessage;
    }
}
