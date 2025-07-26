package com.szr.flashim.example.nettyclient;

import com.google.protobuf.ByteString;
import com.szr.flashim.core.netty.DefaultNettyClientManager;
import com.szr.flashim.core.netty.async.NettyClientSendCallBack;
import com.szr.flashim.core.netty.config.NettyClientConfig;
import com.szr.flashim.core.netty.event.ChannelEventListener;
import com.szr.flashim.core.netty.processor.*;
import com.szr.flashim.core.netty.thread.ThreadFactoryImpl;
import com.szr.flashim.example.model.Message;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.AuthMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.general.model.protoc.HeartBeat;
import com.szr.flashim.general.utils.SequenceIdUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExampleNettyClientManager {
    private final long SEND_TIMEOUT_MILLIS = 30 * 1000;
    private String serverAddr;
    private final DefaultNettyClientManager clientManager;
    private final SequenceIdUtils sequenceIdUtils = SequenceIdUtils.getInstance();
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(new Random().nextLong(0, 1023));

    /**
     * 使用单聊的默认消息处理器，需要传一个处理回调，用于接收到好友的聊天消息后的回调
     *
     * @param serverAddr           服务器地址
     * @param receiveMessageInvoke 消息处理回调
     * @param channelEventListener 连接监听器
     */
    public ExampleNettyClientManager(String serverAddr,
                                     NettyReceiveChatMessageInvoke receiveMessageInvoke,
                                     NettyReceiveAuthNotifyInvoke receiveNotifyInvoke,
                                     ChannelEventListener channelEventListener) {
        // 添加聊天消息处理器，可自行添加，此处使用默认，故需要传入一个消息回调实现
        List<NettyClientRequestProcessor> requestProcessors = new ArrayList<>();
        DefaultChatMessageProcessor chatMessageProcessor = new DefaultChatMessageProcessor(receiveMessageInvoke);
        requestProcessors.add(chatMessageProcessor);

        // 添加消息处理器使用的线程池，不加则使用内置默认创建的公共线程池
        ExecutorService messageExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryImpl("ChatMessageProcessor_"));

        // 添加提醒处理器
        List<NettyNotifyProcessor> nettyNotifyProcessors = new ArrayList<>();
        DefaultAuthNotifyProcessor authNotifyProcessor = new DefaultAuthNotifyProcessor(receiveNotifyInvoke);
        nettyNotifyProcessors.add(authNotifyProcessor);

        // 添加提醒处理器使用的线程池，不加则使用内置默认创建的公共线程池
        ExecutorService notifyExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2,
                new ThreadFactoryImpl("NotifyMessageProcessor_"));

        // 添加连接监听器，如果不加，则不处理客户端与服务端的连接状态变更
        List<ChannelEventListener> channelEventListeners = new ArrayList<>();
        if (channelEventListener != null) {
            channelEventListeners.add(channelEventListener);
        }

        // 添加netty客户端的配置，可自行修改
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        // 120秒的读写空闲
        nettyClientConfig.setClientChannelMaxIdleTimeSeconds(120);

        this.serverAddr = serverAddr;
        this.clientManager = new DefaultNettyClientManager(
                nettyClientConfig,
                requestProcessors,
                messageExecutor,
                nettyNotifyProcessors,
                notifyExecutor,
                channelEventListeners
        );
        this.clientManager.start();
    }

    /**
     * 连接netty服务端，认证通过后，服务端会缓存客户与服务端的连接信息
     *
     * @param uid          用户id
     * @param token        认证令牌
     * @param sendCallBack 发送认证后的回调，对认证结果的处理
     */
    public void connectAsync(long uid, String token, NettyClientSendCallBack sendCallBack) {
        AuthMessage authMessage = AuthMessage.newBuilder()
                .setUid(uid)
                .setToken(token)
                .setCreateTime(System.currentTimeMillis())
                .build();
        ImMessage authRequest = ImMessage.createLoginMessage(idGenerator.nextId(), authMessage);
        this.clientManager.sendMessageAsync(this.serverAddr, authRequest, SEND_TIMEOUT_MILLIS, sendCallBack);

    }

    /**
     * 主动退出连接netty服务端
     *
     * @param uid          用户id
     * @param token        认证令牌
     * @param sendCallBack 发送认证后的回调，对认证结果的处理
     */
    public void disConnectAsync(long uid, String token, NettyClientSendCallBack sendCallBack) {
        AuthMessage authMessage = AuthMessage.newBuilder()
                .setUid(uid)
                .setToken(token)
                .setCreateTime(System.currentTimeMillis())
                .build();
        ImMessage authRequest = ImMessage.createLogoutMessage(idGenerator.nextId(), authMessage);
        this.clientManager.sendMessageAsync(this.serverAddr, authRequest, SEND_TIMEOUT_MILLIS, sendCallBack);
    }

    public void sendChatMessageAsync(final Message message, NettyClientSendCallBack sendMessageCallBack) {
        sendChatMessageAsync(this.serverAddr, message, SEND_TIMEOUT_MILLIS, sendMessageCallBack);
    }

    /**
     * 发送聊天消息
     *
     * @param addr          服务端地址
     * @param message       消息内容
     * @param timeoutMillis 发送超时时间
     * @param sendCallBack  发送回调，对发送结果的处理
     */
    public void sendChatMessageAsync(String addr, final Message message, long timeoutMillis, NettyClientSendCallBack sendCallBack) {
        ChatMessage chatMessage = ChatMessage.newBuilder()
                .setMessageType(message.getMessageType())
                .setMessageContent(ByteString.copyFrom(message.getMessageContent().getBytes()))
                .setMessageFrom(message.getMessageFrom())
                .setMessageFromName(message.getMessageFromName())
                .setMessageTo(message.getMessageTo())
                .setMessageToName(message.getMessageToName())
                .setClientSendTime(message.getClientSendTime())
                .setSessionId(message.getSessionId())
                .setClientSeq(message.getClientSeq())
                .setClientMsgId(message.getClientMsgId())
                .setSourceType(1)
                .setEncryptType(1)
                //.setMessageId(sequenceIdUtils.next())
                //.setMessageId(idGenerator.nextId())
                .build();
        ImMessage request = ImMessage.createSingleChatMessage(idGenerator.nextId(), chatMessage);
        this.clientManager.sendMessageAsync(addr, request, timeoutMillis, sendCallBack);
    }

    public void sendHeartBeatMessageAsync(long userId, NettyClientSendCallBack sendCallBack) {
        HeartBeat heartBeat = HeartBeat.newBuilder().setUid(userId).build();
        ImMessage request = ImMessage.createHeartBeat(idGenerator.nextId(), heartBeat);
        this.clientManager.sendMessageAsync(this.serverAddr, request, SEND_TIMEOUT_MILLIS, sendCallBack);
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public void shutdown() {
        this.clientManager.shutdown();
    }
}
