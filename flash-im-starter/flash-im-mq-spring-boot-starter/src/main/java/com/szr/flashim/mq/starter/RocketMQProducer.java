package com.szr.flashim.mq.starter;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class RocketMQProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQProducer.class);
    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    // 1.同步发送消息
    // 同步发送是指发送方发送一条消息后，会等待服务器返回确认信息后再进行后续操作。这种方式适用于需要可靠性保证的场景。
    public void createAndSend(String topic, RocketMqMessage message) {
        rocketMQTemplate.convertAndSend(topic, message);
    }

    // 1.同步发送消息
    // 同步发送是指发送方发送一条消息后，会等待服务器返回确认信息后再进行后续操作。这种方式适用于需要可靠性保证的场景。
    public SendResult sendSyncMessage(String topic, RocketMqMessage rocketMqMessage) {
        Message<RocketMqMessage> message = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                .build();
        SendResult sendResult = rocketMQTemplate.syncSend(topic, message);

        return sendResult;
    }

    // 2.异步发送消息
    // 异步发送是指发送方发送消息后，不等待服务器返回确认信息，而是通过回调接口处理返回结果。这种方式适用于对响应时间要求较高的场景。
    public void sendAsyncMessage(String topic, RocketMqMessage rocketMqMessage) {
        this.sendAsyncMessage(topic, rocketMqMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }

            @Override
            public void onException(Throwable throwable) {
            }
        });
    }

    public void sendAsyncMessage(String topic, RocketMqMessage rocketMqMessage, SendCallback sendCallback) {
        Message<RocketMqMessage> message = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                .build();

        rocketMQTemplate.asyncSend(topic, message, sendCallback);
    }


    // 3.单向发送消息
    // 单向发送是指发送方只负责发送消息，不关心服务器的响应。该方式适用于对可靠性要求不高的场景，如日志收集。
    public void sendOneWayMessage(String topic, RocketMqMessage rocketMqMessage) {
        Message<RocketMqMessage> message = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                .build();
        rocketMQTemplate.sendOneWay(topic, MessageBuilder.withPayload(message).build());
    }

    /**
     * 4.发送事务消息
     */
    public LocalTransactionState sendTransactionMessage(String topic, RocketMqMessage rocketMqMessage) {
        Message<RocketMqMessage> msg = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                //.setHeader(RocketMQHeaders.TAGS, "TagC")  // 设置Tag过滤
                .build();
        // 发送事务消息
        TransactionSendResult result = rocketMQTemplate.sendMessageInTransaction(topic, msg, null);
        LOGGER.info("事务消息状态：{}", result.getLocalTransactionState());
        return result.getLocalTransactionState();
    }

    public void sendAsyncOrderlyMessage(String topic, RocketMqMessage rocketMqMessage, String hashKey, SendCallback sendCallback) {
        Message<RocketMqMessage> message = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                .build();

        rocketMQTemplate.asyncSendOrderly(topic, message, hashKey, sendCallback);
    }

    public SendResult sendSyncOrderlyMessage(String topic, RocketMqMessage rocketMqMessage, String hashKey) {
        Message<RocketMqMessage> message = MessageBuilder
                .withPayload(rocketMqMessage)
                .setHeader(RocketMQHeaders.KEYS, rocketMqMessage.getId())
                .build();

        return rocketMQTemplate.syncSendOrderly(topic, message, hashKey);
    }

}

