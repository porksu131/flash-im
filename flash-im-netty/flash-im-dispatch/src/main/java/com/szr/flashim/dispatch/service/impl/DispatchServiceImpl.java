package com.szr.flashim.dispatch.service.impl;

import com.flashim.szr.cache.starter.CacheConstant;
import com.flashim.szr.cache.starter.UserSessionManager;
import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.core.netty.async.InvokeCallback;
import com.szr.flashim.core.netty.exception.SendRequestException;
import com.szr.flashim.core.netty.exception.SendTimeoutException;
import com.szr.flashim.dispatch.FlashImDispatchNettyServer;
import com.szr.flashim.dispatch.service.DispatchService;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.*;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import io.netty.channel.Channel;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DispatchServiceImpl implements DispatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchServiceImpl.class);
    private static final long TIMEOUT_MILLIONS = 6000;
    private static final long BATCH_TIMEOUT_MILLIONS = 12000;

    @Autowired
    private FlashImDispatchNettyServer nettyServer;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private UserSessionManager userSessionManager;

    @Autowired
    private MqClientManager mqClientManager;


    @Override
    public void dispatchMessage(RocketMqMessage rocketMqMessage) {
        if (rocketMqMessage.getMqEventType() == MqEventType.SINGLE_CHAT_DISPATCH.getCode()) {
            processDispatchSingleChat(rocketMqMessage);
        } else if (rocketMqMessage.getMqEventType() == MqEventType.BATCH_MSG_DISPATCH.getCode()) {
            // 部分重复代码（待优化）
            processDispatchBatchMessage(rocketMqMessage);
        }
    }

    @Override
    public void dispatchNotify(RocketMqMessage rocketMqMessage) {
        if (rocketMqMessage.getMqEventType() == MqEventType.USER_ONLINE_CHANGE_NOTIFY.getCode()) {
            processDispatchFriendNotify(rocketMqMessage);
        }
    }


    /**
     * 推送好友通知
     * @param rocketMqMessage 消息
     */
    public void processDispatchFriendNotify(RocketMqMessage rocketMqMessage) {
        try {
            BatchFriendNotify batchFriendNotify = BatchFriendNotify.parseFrom(rocketMqMessage.getContent());
            List<Long> friendIds = batchFriendNotify.getFriendIdsList();
            if (friendIds.isEmpty()) {
                return;
            }
            // 推送所有在线的好友
            for (Long friendId : friendIds) {
                Channel channel = getAndCheckChannel(friendId);
                if (channel != null) {
                    // 转发通知
                    dispatchFriendNotify(channel, friendId, batchFriendNotify);
                    LOGGER.info("好友[{}]在线，已转发通知", friendId);
                }
            }

        } catch (Exception e) {
            LOGGER.error("推送用户上线/离线通知给其好友失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量信息转发
     *
     * @param rocketMqMessage 消息
     */
    public void processDispatchBatchMessage(RocketMqMessage rocketMqMessage) {
        try {
            BatchMessage batchMessage = BatchMessage.parseFrom(rocketMqMessage.getContent());
            long toUid = batchMessage.getMessageTo();
            Channel channel = getAndCheckChannel(toUid);
            if (channel != null) {
                // 消息转发
                dispatchBatchMessageToUser(channel, batchMessage);
                return;
            }
            LOGGER.info("批量消息转发失败，目标用户[{}]不在线", toUid);
            // 目标用户离线
            pushMessageToMq(MqEventType.BATCH_MSG_DISPATCH_FAIL, batchMessage);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("批量消息转发发生异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 单聊信息转发
     *
     * @param rocketMqMessage 消息
     */
    public void processDispatchSingleChat(RocketMqMessage rocketMqMessage) {
        try {
            ChatMessage chatMessage = ChatMessage.parseFrom(rocketMqMessage.getContent());
            long toUid = chatMessage.getMessageTo();
            Channel channel = getAndCheckChannel(toUid);
            if (channel != null) {
                // 消息转发
                dispatchMessageToUser(channel, chatMessage, rocketMqMessage, toUid);
                return;
            }
            LOGGER.info("消息[{}]已处理，目标用户[{}]不在线", chatMessage.getMessageId(), toUid);
            // 目标用户离线
            pushMessageToMq(MqEventType.SINGLE_CHAT_DISPATCH_FAIL, chatMessage, rocketMqMessage);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("消息转发发生异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void dispatchFriendNotify(Channel channel, Long friendId, BatchFriendNotify batchFriendNotify)
            throws SendRequestException, SendTimeoutException, InterruptedException {
        FriendNotify friendNotify = FriendNotify.newBuilder()
                .setUid(batchFriendNotify.getUid())
                .setOperationType(batchFriendNotify.getOperationType())
                .setOperationTime(batchFriendNotify.getOperationTime())
                .setFriendId(friendId)
                .build();
        ImMessage imMessage = ImMessage.createOfflineNotify(snowflakeIdGenerator.nextId(), friendNotify);
        nettyServer.getMessageProcessManager().invokeOneway(channel, imMessage, TIMEOUT_MILLIONS);
    }

    public void dispatchBatchMessageToUser(Channel channel, BatchMessage batchMessage) {
        ImMessage imMessage = ImMessage.createBatchMessage(snowflakeIdGenerator.nextId(), batchMessage);
        InvokeCallback sendCallback = createBatchSendCallback(batchMessage);
        nettyServer.getMessageProcessManager().sendAsync(channel, imMessage, BATCH_TIMEOUT_MILLIONS, sendCallback);
    }

    public void dispatchMessageToUser(Channel channel, ChatMessage chatMessage, RocketMqMessage message, long toUserId) {
        ImMessage imMessage = ImMessage.createSingleChatMessage(snowflakeIdGenerator.nextId(), chatMessage);
        InvokeCallback sendCallback = createBatchSendCallback(chatMessage, message, toUserId);
        nettyServer.getMessageProcessManager().sendAsync(channel, imMessage, TIMEOUT_MILLIONS, sendCallback);
    }

    private InvokeCallback createBatchSendCallback(ChatMessage chatMessage, RocketMqMessage message, long toUserId) {
        return new InvokeCallback() {
            @Override
            public void operationSucceed(ImMessage response) {
                try {
                    CommonResponse commonResponse = CommonResponse.parseFrom(response.getBody());
                    if (ResponseCode.SUCCESS == commonResponse.getCode()) {
                        LOGGER.info("消息[{}]转发到用户[{}]成功", chatMessage.getMessageId(), toUserId);
                        pushMessageToMq(MqEventType.SINGLE_CHAT_DISPATCH_SUCCESS, chatMessage, message); // 推送->已转发成功
                        return;
                    }
                    LOGGER.error("消息[{}]转发到用户[{}]失败:{}", chatMessage.getMessageId(), toUserId, commonResponse.getMsg());
                    pushMessageToMq(MqEventType.SINGLE_CHAT_DISPATCH_FAIL, chatMessage, message); // 推送->目标用户离线

                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void operationFail(Throwable throwable) {
                LOGGER.error("消息转发用户异常：{}", throwable.getMessage());
                pushMessageToMq(MqEventType.SINGLE_CHAT_DISPATCH_FAIL, chatMessage, message); // 当作离线处理
            }
        };
    }

    private InvokeCallback createBatchSendCallback(BatchMessage batchMessage) {
        return new InvokeCallback() {
            @Override
            public void operationSucceed(ImMessage response) {
                try {
                    CommonResponse commonResponse = CommonResponse.parseFrom(response.getBody());
                    if (ResponseCode.SUCCESS == commonResponse.getCode()) {
                        LOGGER.info("批量消息转发到用户[{}]成功", batchMessage.getMessageTo());
                        pushMessageToMq(MqEventType.BATCH_MSG_DISPATCH_SUCCESS, batchMessage); // 推送->已转发成功
                        return;
                    }
                    LOGGER.error("批量消息转发到用户[{}]失败:{}", batchMessage.getMessageTo(), commonResponse.getMsg());
                    pushMessageToMq(MqEventType.BATCH_MSG_DISPATCH_FAIL, batchMessage); // 推送->转发失败

                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void operationFail(Throwable throwable) {
                LOGGER.error("批量消息转发用户异常：{}", throwable.getMessage());
                pushMessageToMq(MqEventType.BATCH_MSG_DISPATCH_FAIL, batchMessage); // 当作离线处理
            }
        };
    }


    public void pushMessageToMq(MqEventType mqEventType, ChatMessage chatMessage, RocketMqMessage message) {
        String topic = switchTopic(mqEventType);

        mqClientManager.sendAsyncMessage(topic, mqEventType, message.getContent(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                LOGGER.info("消息[{}]已推送消息队列:[{}], [{}]", chatMessage.getMessageId(), topic, mqEventType);
            }

            @Override
            public void onException(Throwable throwable) {
                LOGGER.error("消息[{}]推送失败:[{}],[{}], 原因：[{}]", chatMessage.getMessageId(), topic, mqEventType, throwable.getMessage());
            }
        });
    }

    public void pushMessageToMq(MqEventType mqEventType, BatchMessage batchMessage) {
        String topic = switchTopic(mqEventType);
        BatchMessageIds batchMessageIds = BatchMessageIds.newBuilder()
                .setMessageTo(batchMessage.getMessageTo())
                .addAllChatMessageIds(batchMessage.getChatMessagesList().stream()
                        .map(ChatMessage::getMessageId).collect(Collectors.toList()))
                .build();

        mqClientManager.sendAsyncMessage(topic, mqEventType, batchMessageIds.toByteArray(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                LOGGER.info("批量消息已推送消息队列:[{}], [{}]", topic, mqEventType);
            }

            @Override
            public void onException(Throwable throwable) {
                LOGGER.error("批量消息推送失败:[{}],[{}], 原因：[{}]", topic, mqEventType, throwable.getMessage());
            }
        });
    }

    private Channel getAndCheckChannel(long uid) {
        Map<String, Object> sessionInfo = userSessionManager.getUserRedisSession(uid);
        if (sessionInfo != null && sessionInfo.get("gatewayAddress") != null) {
            String gatewayAddress = sessionInfo.get("gatewayAddress").toString();
            Channel channel = nettyServer.getChannelCacheManager().getActiveChannel(gatewayAddress);
            if (channel != null && channel.isActive()) {
                return channel;
            }
        }
        return null;
    }

    private String switchTopic(MqEventType mqEventType) {
        String topic = "";
        switch (mqEventType) {
            case SINGLE_CHAT_DISPATCH_SUCCESS:
            case BATCH_MSG_DISPATCH_SUCCESS:
                topic = MqTopicConstant.SING_CHAT_TOPIC;
                break;
            case SINGLE_CHAT_DISPATCH_FAIL:
            case BATCH_MSG_DISPATCH_FAIL:
                topic = MqTopicConstant.MSG_DISPATCH_FAIL_TOPIC;
                break;
            default:
        }

        return topic;
    }
}
