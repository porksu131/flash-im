package com.szr.flashim.dispatch.processor;

import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.core.netty.processor.NettyRequestProcessor;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.MsgType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatchProcessor implements NettyRequestProcessor {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DispatchProcessor.class);

    @Autowired
    private MqClientManager mqClientManager;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public int bizType() {
        return BizType.SINGLE_CHAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(MsgType.REQUEST.equals(request.getMsgType()) && BizType.SINGLE_CHAT.equals(request.getBizType()));
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) throws InvalidProtocolBufferException {
        ChatMessage chatMessage = ChatMessage.parseFrom(request.getBody());

        // 赋值序列号
        ChatMessage newChatMessage = chatMessage.toBuilder()
                .setMessageId(snowflakeIdGenerator.nextId())
                .setSequenceId(snowflakeIdGenerator.nextId())
                .build();
        // 同一组会话消息发往同一队列
        SendResult sendResult = mqClientManager.sendSyncOrderlyMessage(
                MqTopicConstant.SING_CHAT_TOPIC + ":chat-event",
                MqEventType.SINGLE_CHAT_BEGIN,
                newChatMessage.toByteArray(),
                newChatMessage.getSessionId());

        if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
            LOGGER.info("消息[{}]已推送消息队列，待消息转发", request.getMsgId());
            return ImMessage.createMessageResponse(request, ResponseCode.SUCCESS, "消息已成功发往消息队列，待消息转发", newChatMessage.toByteArray());
        }
        LOGGER.error("消息[{}]推送失败:{}", request.getMsgId(), sendResult.getSendStatus());
        return ImMessage.createMessageResponse(request, ResponseCode.SYSTEM_ERROR, "消息发往消息队列失败:" + sendResult.getSendStatus());
    }
}
