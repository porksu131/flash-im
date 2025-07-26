package com.szr.flashim.example.gui.chat;

import com.google.protobuf.InvalidProtocolBufferException;
import com.szr.flashim.core.netty.async.NettyClientSendCallBack;
import com.szr.flashim.example.model.Message;
import com.szr.flashim.example.nettyclient.ExampleNettyClientManager;
import com.szr.flashim.example.sqlite.DatabaseManager;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.general.model.protoc.CommonResponse;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MessageSender {
    private static final long MESSAGE_TIMEOUT = 15000; // 30秒超时
    private static final long MAX_RETRY_COUNT = 1;     // 最大重试次数 todo
    
    private final AtomicLong nextClientSeq = new AtomicLong(1);
    private final BlockingQueue<Message> sendQueue = new PriorityBlockingQueue<>(11,
        Comparator.comparingLong(Message::getClientSeq));

    // 当前待ack确认的消息
    private Message pendingMessage;
    // 所有当前待ack确认的消息，需要等待上一条确认后，才发送到netty
    private final ConcurrentMap<Long, Message> pendingMessages = new ConcurrentHashMap<>();
    // 定时检查超时的消息
    private final ScheduledExecutorService timeoutChecker = Executors.newSingleThreadScheduledExecutor();
    
    private final ExampleNettyClientManager nettyClientManager;
    private final DatabaseManager dbManager;
    private final MessageListModel listModel;
    private Consumer<Message> statusUpdater;


    public MessageSender(ExampleNettyClientManager nettyClientManager,
                         DatabaseManager dbManager,
                         MessageListModel listModel) {
        this.nettyClientManager = nettyClientManager;
        this.dbManager = dbManager;
        this.listModel = listModel;
        // 启动超时检查任务
        timeoutChecker.scheduleAtFixedRate(this::checkMessagesTimeout, 1, 1, TimeUnit.SECONDS);
    }

    public void initializeClientSeq(long maxSeq) {
        if (maxSeq > 0) {
            nextClientSeq.set(maxSeq);
        }
    }

    public void sendMessage(Message sendMsg) {
        // 序列号+1
        sendMsg.setClientSeq(nextClientSeq.getAndIncrement() + 1);
        // 保存到数据库并添加到UI
        dbManager.saveMessage(sendMsg);
        listModel.addMessage(sendMsg);
        
        // 如果没有待处理消息，立即发送
        if (pendingMessage == null) {
            sendImmediately(sendMsg);
        } else {
            sendQueue.add(sendMsg);
        }
    }

    private void sendImmediately(Message msg) {
        pendingMessage = msg;
        pendingMessages.put(msg.getClientMsgId(), msg);
        SwingUtilities.invokeLater(() -> {
            sendTxtMessageToServer(msg);
        });
    }

    public void handleAck(ChatMessage ackMessage) {
        long sequenceId = ackMessage.getSequenceId();
        long messageId = ackMessage.getMessageId();
        Message msg = pendingMessages.remove(ackMessage.getClientMsgId());
        if (msg != null) {
            // 更新消息状态
            msg.setMessageId(messageId);
            msg.setSequenceId(sequenceId);
            msg.setSendStatus(Message.SEND_STATUS_SUCCESS);
            msg.setRetryCount(0);  // 重置重试计数
            
            // 更新数据库和UI
            dbManager.saveMessage(msg);
            statusUpdater.accept(msg);
            
            // 处理下一条消息
            pendingMessage = sendQueue.poll();
            if (pendingMessage != null) {
                sendImmediately(pendingMessage);
            }
        }
    }
    
    public void checkMessagesTimeout() {
        long currentTime = System.currentTimeMillis();
        
        // 检查pending消息
        for (Message msg : pendingMessages.values()) {
            if (currentTime - msg.getClientSendTime() > MESSAGE_TIMEOUT) {
                handleMessageTimeout(msg);
            }
        }
    }
    
    private void handleMessageTimeout(Message msg) {
        if (msg.getRetryCount() < MAX_RETRY_COUNT) {
            // 自动重试
            msg.setRetryCount(msg.getRetryCount() + 1);
            msg.setClientSendTime( System.currentTimeMillis());// 更新重试时间
            dbManager.saveMessage(msg);
            statusUpdater.accept(msg);
            
            // 重新发送
            if (pendingMessage == null || pendingMessage.getClientSeq() == msg.getClientSeq()) {
                sendImmediately(msg);
            }
        } else {
            // 标记为失败
            msg.setSendStatus(Message.SEND_STATUS_FAILURE);
            dbManager.saveMessage(msg);
            statusUpdater.accept(msg);
            pendingMessages.remove(msg.getClientMsgId());
            
            // 如果是当前pending消息，处理下一条
            if (pendingMessage != null && pendingMessage.getClientSeq() == msg.getClientSeq()) {
                pendingMessage = sendQueue.poll();
                if (pendingMessage != null) {
                    sendImmediately(pendingMessage);
                }
            }
        }
    }

    // 单条重发
    public void resendMessage(Message msg) {
        if (msg.getSendStatus() == Message.SEND_STATUS_FAILURE) {
            // 重置状态
            msg.setSendStatus(Message.SEND_STATUS_SENDING);
            msg.setRetryCount(0);
            msg.setClientSendTime(System.currentTimeMillis());
            
            // 更新数据库和UI
            dbManager.saveMessage(msg);
            statusUpdater.accept(msg);
            
            // 重新加入发送流程
            if (pendingMessage == null) {
                sendImmediately(msg);
            } else if (msg.getClientSeq() < pendingMessage.getClientSeq()) {
                // 优先级高于当前消息，需要特殊处理
                sendQueue.add(pendingMessage);
                sendImmediately(msg);
            } else {
                sendQueue.add(msg);
            }
        }
    }

    // 批量重发
    public void resendAllFailedMessages(List<Message> failedMessages) {
        if (failedMessages.isEmpty()) return;
        failedMessages.sort(Comparator.comparingLong(Message::getClientSeq));

        for (Message msg : failedMessages) {
            resendMessage(msg);
        }
    }

    private void sendTxtMessageToServer(Message sendMsg) {
        nettyClientManager.sendChatMessageAsync(sendMsg, new NettyClientSendCallBack() {
            @Override
            public void onSendSuccess(CommonResponse response) {
                SwingUtilities.invokeLater(() -> {
                    if (ResponseCode.SUCCESS == response.getCode()) {
                        try {
                            ChatMessage ackMessage = ChatMessage.parseFrom(response.getData().toByteArray());
                            handleAck(ackMessage);
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e);
                        }
                    }else {
                        sendMsg.setSendStatus(Message.SEND_STATUS_FAILURE);
                        statusUpdater.accept(sendMsg);
                    }
                });
            }

            @Override
            public void onSendFail(Throwable throwable) {
                SwingUtilities.invokeLater(() -> {
                    sendMsg.setSendStatus(Message.SEND_STATUS_FAILURE);
                    statusUpdater.accept(sendMsg);
                });
            }
        });
    }

    // 设置器方法
    public void setStatusUpdater(Consumer<Message> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void shutdown() {
        timeoutChecker.shutdown();
    }
}
