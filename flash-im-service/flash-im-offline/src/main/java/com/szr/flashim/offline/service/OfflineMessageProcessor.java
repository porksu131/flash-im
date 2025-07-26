package com.szr.flashim.offline.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.general.model.protoc.AuthNotify;
import com.szr.flashim.general.model.protoc.BatchMessageIds;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.RocketMqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfflineMessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineMessageProcessor.class);

    @Autowired
    private OfflineMessageService offlineMessageService;

    // 收到离线消息
    public void processMqMessage(RocketMqMessage rocketMqMessage) throws InvalidProtocolBufferException {
        if (MqEventType.SINGLE_CHAT_DISPATCH_FAIL.equals(rocketMqMessage.getMqEventType())) {
            ChatMessage chatMessage = ChatMessage.parseFrom(rocketMqMessage.getContent());
            LOGGER.info("收到用户[{}]转发失败的消息，添加用户的离线消息到缓存", chatMessage.getMessageTo());
            // 保存离线消息到redis
            offlineMessageService.addOfflineMessage(chatMessage.getMessageTo(), chatMessage.getMessageId());
        } else if (MqEventType.BATCH_MSG_DISPATCH_FAIL.equals(rocketMqMessage.getMqEventType())) {
            BatchMessageIds batchMessageIds = BatchMessageIds.parseFrom(rocketMqMessage.getContent());
            LOGGER.info("收到用户[{}]批量转发失败的消息，重新添加用户的批量离线消息到缓存", batchMessageIds.getMessageTo());
            // 保存离线消息到redis（批量的，待优化）
            offlineMessageService.addOfflineMessage(batchMessageIds.getMessageTo(), batchMessageIds.getChatMessageIdsList());
        }

        throw new RuntimeException("unknown rocket mq event type");
    }
}