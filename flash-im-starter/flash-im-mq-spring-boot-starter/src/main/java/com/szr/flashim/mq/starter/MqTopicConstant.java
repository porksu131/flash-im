package com.szr.flashim.mq.starter;

public class MqTopicConstant {
//    // 用户认证通过后连接成功（dispatch-->微服务）
//    public static final String USER_CONNECTED_TOPIC = "user-connected-topic";
    // 用户单聊信息
    public static final String SING_CHAT_TOPIC = "sing-chat-topic";
    // 用户群聊信息
    public static final String GROUP_CHAT_TOPIC = "group-chat-topic";
    // （单个）分发信息（当用户发送的消息持久化后，由单聊微服务发出转发消息通知，netty分发服务接收）
    public static final String MSG_DISPATCH_SINGLE_SEND_TOPIC = "msg-dispatch-single-send-topic";
    // （批量）分发信息（当用户发送的消息持久化后，由离线微服务发出转发消息通知，netty分发服务接收）
    public static final String MSG_DISPATCH_BATCH_SEND_TOPIC = "msg-dispatch-batch-send-topic";
    // 消息分发成功（当用户发送的消息成功转发到另一用户后，netty分发服务通知单聊微服务更新消息状态）
    public static final String MSG_DISPATCH_SUCCESS_TOPIC = "msg-dispatch-success-topic";
    // 离线消息（用户转发消息失败，netty分发服务通知离线消息微服务，做离线消息保存）
    public static final String MSG_DISPATCH_FAIL_TOPIC = "msg-dispatch-fail-topic";
    // 好友通知 推送用户在线状态变更通知给好友
    public static final String NOTIFY_DISPATCH_TOPIC = "notify-dispatch-topic";
    // 用户在线状态变更
    public static final String USR_AUTH_CHANGE_TOPIC = "user-auth-change-topic";
}
