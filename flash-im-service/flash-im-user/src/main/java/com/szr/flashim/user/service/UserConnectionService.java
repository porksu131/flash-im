package com.szr.flashim.user.service;

import com.flashim.szr.cache.starter.CacheConstant;
import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.general.constant.OnlineStatus;
import com.szr.flashim.general.enumeration.NotifyType;
import com.szr.flashim.general.model.protoc.AuthNotify;
import com.szr.flashim.general.model.protoc.BatchFriendNotify;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import com.szr.flashim.mq.starter.RocketMqMessage;
import com.szr.flashim.user.mapper.FriendRelationMapper;
import com.szr.flashim.user.pojo.FriendRelation;
import com.szr.flashim.user.vo.FriendStatus;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserConnectionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserConnectionService.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MqClientManager mqClientManager;

    @Autowired
    private FriendRelationMapper friendRelationMapper;

    // 检查用户是否在线
    public boolean isOnline(Long userId) {
        Integer status = (Integer) redisTemplate.opsForValue().get(CacheConstant.USER_ONLINE_STATUS + userId);
        return status != null && OnlineStatus.ONLINE == status;
    }

    // 获取用户最后活跃时间
    public Long getLastActiveTime(Long userId) {
        return (Long) redisTemplate.opsForValue().get(CacheConstant.USER_ACTIVE_TIME + userId);
    }

    // 批量获取用户状态
    public Map<Long, Integer> batchCheckOnline(List<Long> userIds) {
        List<String> keys = userIds.stream()
                .map(id -> CacheConstant.USER_ONLINE_STATUS + id)
                .collect(Collectors.toList());

        List<Object> statuses = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Integer> result = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            Integer status = (Integer) statuses.get(i);
            result.put(userIds.get(i), status);
        }
        return result;
    }

    // 批量获取用户状态和活跃时间
    public Map<Long, FriendStatus> batchGetStatus(List<Long> userIds) {
        List<String> statusKeys = userIds.stream()
                .map(id -> CacheConstant.USER_ONLINE_STATUS + id)
                .collect(Collectors.toList());

        List<String> activeKeys = userIds.stream()
                .map(id -> CacheConstant.USER_ACTIVE_TIME + id)
                .collect(Collectors.toList());

        List<Object> statusValues = redisTemplate.opsForValue().multiGet(statusKeys);
        List<Object> activeValues = redisTemplate.opsForValue().multiGet(activeKeys);

        Map<Long, FriendStatus> result = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            Integer onlineStatus = (Integer) statusValues.get(i);
            Long activeTime = (Long) activeValues.get(i);

            FriendStatus friendStatus = new FriendStatus();
            friendStatus.setOnlineStatus(onlineStatus);
            friendStatus.setLastActiveTime(activeTime);

            result.put(userIds.get(i), friendStatus);
        }
        return result;
    }

    // 当用户在线状态变更，推送其状态给所有朋友
    public void processMqMessage(RocketMqMessage rocketMqMessage) throws InvalidProtocolBufferException {
        if (MqEventType.USER_ONLINE_CHANGE_NOTIFY.equals(rocketMqMessage.getMqEventType())) {
            AuthNotify authNotify = AuthNotify.parseFrom(rocketMqMessage.getContent());
            pushStatusChangeToAllFriends(authNotify);
        }
    }

    private void pushStatusChangeToAllFriends(AuthNotify authNotify) {
        long userId = authNotify.getUid();
        boolean isLogin = NotifyType.ON_LINE.getCode() == authNotify.getOperationType();
        LOGGER.debug("收到用户[{}]{}通知，开始推送给其所有好友", userId, isLogin ? "上线" : "离线");
        // 1. 查询好友关系
        List<FriendRelation> relations = friendRelationMapper.findFriendsByUid(userId);
        if (relations == null || relations.isEmpty()) {
            return;
        }

        // 2. 提取好友ID
        List<Long> friendIds = relations.stream()
                .map(FriendRelation::getFriendId)
                .toList();

        // 3. 消息推送，推送给所有好友
        BatchFriendNotify batchFriendNotify = BatchFriendNotify.newBuilder()
                .setUid(userId)
                .setOperationType(authNotify.getOperationType())
                .setOperationTime(authNotify.getOperationTime())
                .addAllFriendIds(friendIds)
                .build();

        mqClientManager.sendAsyncMessage(MqTopicConstant.NOTIFY_DISPATCH_TOPIC,
                MqEventType.USER_ONLINE_CHANGE_NOTIFY, batchFriendNotify.toByteArray(), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        LOGGER.info("已推送用户[{}][{}]消息给其所有好友", userId, isLogin ? "上线" : "离线");
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        LOGGER.warn("推送用户[{}][{}]消息给其所有好友失败，原因：{}", userId, isLogin ? "上线" : "离线", throwable.getMessage());
                    }
                });
    }


}
