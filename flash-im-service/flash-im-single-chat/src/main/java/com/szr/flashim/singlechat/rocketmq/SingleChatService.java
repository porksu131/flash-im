package com.szr.flashim.singlechat.rocketmq;

import com.flashim.szr.cache.starter.SequenceIdGenerator;
import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.model.protoc.BatchMessageIds;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import com.szr.flashim.singlechat.contants.MessageStatus;
import com.szr.flashim.singlechat.mapper.ChatMessageMapper;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SingleChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleChatService.class);
    private static final int BATCH_UPDATE_SIZE = 500;

    @Autowired
    private MqClientManager mqClientManager;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public void processMqMessage(RocketMqMessage rocketMqMessage) {
        if (MqEventType.SINGLE_CHAT_BEGIN.equals(rocketMqMessage.getMqEventType())) {
            // 收到单聊消息，消息初始状态为：未发送，推送后：已发送
            saveAndDispatchMsg(rocketMqMessage);
        } else if (MqEventType.SINGLE_CHAT_DISPATCH_SUCCESS.equals(rocketMqMessage.getMqEventType())) {
            // 收到单聊消息已成功转发到用户， 更新单条消息：已读
            updateMsgStatus(rocketMqMessage, MessageStatus.ALREADY_READ);
        } else if (MqEventType.BATCH_MSG_DISPATCH_SUCCESS.equals(rocketMqMessage.getMqEventType())) {
            // 收到批量离线消息已转发到用户，批量更新消息的状态为：已读
            updateBachMsgStatus(rocketMqMessage);
        }
    }

    private void updateBachMsgStatus(RocketMqMessage rocketMqMessage) {
        try {
            BatchMessageIds batchMessage = BatchMessageIds.parseFrom(rocketMqMessage.getContent());
            Long messageTo = batchMessage.getMessageTo();
            List<Long> chatMessageIds = batchMessage.getChatMessageIdsList();
            if (!chatMessageIds.isEmpty()) {
                batchMarkAsRead(chatMessageIds, messageTo);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void saveAndDispatchMsg(RocketMqMessage rocketMqMessage) {
        try {
            ChatMessage chatMessage = ChatMessage.parseFrom(rocketMqMessage.getContent());

            // 消息入库
            saveMessage(chatMessage);
            LOGGER.info("消息[{}]已入库", chatMessage.getMessageId());

            long toUid = chatMessage.getMessageTo();
            // 消息推送到接收者
            pushToMq(chatMessage, toUid);

        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public void pushToMq(ChatMessage newChatMessage, long toUid) {
        // 消息推送到rocketMq
        mqClientManager.sendAsyncMessage(MqTopicConstant.MSG_DISPATCH_SINGLE_SEND_TOPIC,
                MqEventType.SINGLE_CHAT_DISPATCH, newChatMessage.toByteArray(), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        LOGGER.info("消息[{}]已推送，待转发到用户[{}]", newChatMessage.getMessageId(), toUid);
                        updateMsgStatus(newChatMessage.getMessageId(), MessageStatus.ALREADY_SEND);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        LOGGER.warn("消息[{}]推送失败，待用户[{}]自行拉取，原因：{}", newChatMessage.getMessageId(), toUid, throwable.getMessage());
                    }
                });
    }

    public void updateMsgStatus(RocketMqMessage rocketMqMessage, int status) {
        try {
            ChatMessage chatMessage = ChatMessage.parseFrom(rocketMqMessage.getContent());
            // 更新消息状态
            updateMessageStatus(chatMessage.getMessageId(), status);
            LOGGER.info("消息[{}]已更新为已读", chatMessage.getMessageId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMsgStatus(Long messageId, int status) {
        try {
            // 更新消息状态
            updateMessageStatus(messageId, status);
            LOGGER.info("消息[{}]已更新为已读", messageId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void saveMessage(ChatMessage chatMessage) {
        com.szr.flashim.singlechat.pojo.ChatMessage message = new com.szr.flashim.singlechat.pojo.ChatMessage();
        message.setMessageContent(new String(chatMessage.getMessageContent().toByteArray()));
        message.setMessageFrom(chatMessage.getMessageFrom());
        message.setMessageFromName(chatMessage.getMessageFromName());
        message.setMessageId(chatMessage.getMessageId());
        message.setMessageTo(chatMessage.getMessageTo());
        message.setMessageToName(chatMessage.getMessageToName());
        message.setMessageType(chatMessage.getMessageType());
        message.setStatus(MessageStatus.UN_SEND);
        message.setClientSendTime(chatMessage.getClientSendTime());
        message.setClientSeq(chatMessage.getClientSeq());
        message.setSequenceId(chatMessage.getSequenceId());
        message.setSessionId(chatMessage.getSessionId());
        message.setClientMsgId(chatMessage.getClientMsgId());
        chatMessageMapper.insert(message);
    }

    public void updateMessageStatus(long messageId, int status) {
        chatMessageMapper.updateStatus(messageId, status);
    }

    /**
     * 批量更新消息状态
     *
     * @param messageIds 消息ID列表
     * @param messageTo  接收者ID（用于安全校验）
     */
    public void batchMarkAsRead(List<Long> messageIds, Long messageTo) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        Date readTime = new Date();
        int total = messageIds.size();

        // 数量较少的话，不用临时表
        if (messageIds.size() < 100) {
            chatMessageMapper.batchUpdateStatus(messageIds, messageTo, MessageStatus.ALREADY_READ, readTime.getTime());
            LOGGER.info("批量消息状态更新完成: receiver={}", messageTo);
            return;
        }

        // 分批更新（防止SQL语句过长）
        for (int i = 0; i < total; i += BATCH_UPDATE_SIZE) {
            int toIndex = Math.min(i + BATCH_UPDATE_SIZE, total);
            List<Long> batchIds = messageIds.subList(i, toIndex);

            // 更新
            chatMessageMapper.batchUpdateStatusWithTempTable(batchIds, messageTo, MessageStatus.ALREADY_READ, readTime.getTime());
        }

        LOGGER.info("批量消息状态更新完成: receiver={}", messageTo);
    }
}
