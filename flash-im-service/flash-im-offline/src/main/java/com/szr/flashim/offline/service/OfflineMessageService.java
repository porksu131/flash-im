package com.szr.flashim.offline.service;

import com.flashim.szr.cache.starter.CacheConstant;
import com.google.protobuf.ByteString;
import com.szr.flashim.general.model.protoc.BatchMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.offline.mapper.ChatMessageMapper;
import com.szr.flashim.offline.pojo.ChatMessagePojo;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OfflineMessageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineMessageService.class);
    private static final int BATCH_SIZE = 200; // 每批处理数量

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MqClientManager mqClientManager;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    // 添加离线消息ID
    public void addOfflineMessage(Long messageTo, List<Long> messageIds) {
        if (messageIds.isEmpty() || messageTo == null) {
            return;
        }
        for (Long messageId : messageIds) {
            addOfflineMessage(messageTo, messageId);
        }
    }

    // 添加离线消息ID
    public void addOfflineMessage(Long userId, Long messageId) {
        String key = CacheConstant.OFFLINE_MSG_ID_LIST + userId;
        double score = System.currentTimeMillis(); // 使用时间戳作为分数，即时转发失败追加回来了，也会先处理这些刚追加进来的

        redisTemplate.opsForZSet().add(key, messageId.toString(), score);
        // 设置30天过期
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    // 批量获取并删除消息ID
    public List<String> pollBatchMessages(Long userId) {
        String key = CacheConstant.OFFLINE_MSG_ID_LIST + userId;

        // 获取最早的一批消息ID
        Set<String> messageIds = redisTemplate.opsForZSet().range(key, 0, BATCH_SIZE - 1);
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用Lua脚本原子性删除
        String luaScript = "local ids = redis.call('zrange', KEYS[1], 0, ARGV[1]-1) " +
                "if #ids > 0 then " +
                "   redis.call('zremrangebyrank', KEYS[1], 0, #ids-1) " +
                "end " +
                "return ids";

        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, List.class),
                Collections.singletonList(key),
                String.valueOf(BATCH_SIZE)
        );

//        // 管道批量删除
//        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//            for (String msgId : messageIds) {
//                connection.zRem(key.getBytes(), msgId.getBytes());
//            }
//            return null;
//        });

        return messageIds.stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Async("offlineMessageExecutor")
    public void processUserOfflineMessages(Long toUid) {
        List<String> messageIds;

        // 1. 从Redis批量获取消息ID
        messageIds = pollBatchMessages(toUid);
        if (messageIds.isEmpty()) return;

        // 2. 分页查询数据库（防止SQL过长）
        List<ChatMessagePojo> messages = new ArrayList<>();
        for (int i = 0; i < messageIds.size(); i += 500) {
            List<String> subIds = messageIds.subList(i, Math.min(i + 500, messageIds.size()));
            messages.addAll(chatMessageMapper.batchSelectMessages(subIds));
        }

        if (messages.isEmpty()) {
            LOGGER.warn("数据库和redis中存在消息id不一致");
            return;
        }

        // 3. 批量消息推送到rocketMq
        BatchMessage batchMessage = buildBatchMessage(toUid, messages);
        List<String> finalMessageIds = messageIds;
        mqClientManager.sendAsyncMessage(MqTopicConstant.MSG_DISPATCH_SINGLE_SEND_TOPIC, MqEventType.BATCH_MSG_DISPATCH, batchMessage.toByteArray(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                LOGGER.info("离线消息已推送，待转发到用户[{}]", toUid);
            }

            @Override
            public void onException(Throwable throwable) {
                LOGGER.warn("离线消息推送失败，原因：{}", throwable.getMessage());
                // 将消息ID重新加回Redis，等用户自行重新触发推送
                finalMessageIds.forEach(id -> addOfflineMessage(toUid, Long.valueOf(id)));
            }
        });
    }

    private BatchMessage buildBatchMessage(Long toUid, List<ChatMessagePojo> chatMessagePojoList) {
        // 消息类型转换
        List<ChatMessage> chatMessageList = chatMessagePojoList.stream().map(obj -> {
            return ChatMessage.newBuilder()
                    .setMessageId(obj.getMessageId())
                    .setMessageContent(ByteString.copyFrom(obj.getMessageContent().getBytes()))
                    .setMessageFrom(obj.getMessageFrom())
                    .setMessageFromName(obj.getMessageFromName())
                    .setMessageTo(obj.getMessageTo())
                    .setMessageToName(obj.getMessageToName())
                    .setClientSendTime(obj.getCreateTime())
                    .setMessageType(obj.getMessageType())
                    .setClientSeq(obj.getClientSeq())
                    .setClientMsgId(obj.getClientMsgId())
                    .setSessionId(obj.getSessionId())
                    .setSequenceId(obj.getSequenceId())
                    .setStatus(obj.getStatus())
                    .setSourceType(1) // 暂没用到
                    .setEncryptType(1) // 暂没用到
                    .build();
        }).collect(Collectors.toList());

        // 构建消息体
        return BatchMessage.newBuilder()
                .setMessageTo(toUid)
                .addAllChatMessages(chatMessageList)
                .build();
    }
}