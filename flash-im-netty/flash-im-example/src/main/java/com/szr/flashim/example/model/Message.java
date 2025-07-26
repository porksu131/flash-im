package com.szr.flashim.example.model;

public class Message {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = -1;

    public static final int SEND_STATUS_SENDING = 1;
    public static final int SEND_STATUS_SUCCESS = 2;
    public static final int SEND_STATUS_FAILURE = 3;

    private Long messageId; //  服务端消息唯一id snowflake
    private String messageContent; //  消息内容
    private long messageTo; //  消息接收者id
    private String messageToName; //  消息接收者名称
    private long messageFrom; //  消息发送者id
    private String messageFromName; //  消息发送者名称
    private int messageType; //  消息类型 0:文字消息 1:图片消息 2:语音消息
    private long clientMsgId; //  客户端消息id
    private long clientSendTime; //  客户端消息发送时间
    private long clientSeq; // 客户端临时生成的本地序列号
    private Long sequenceId; // 服务器分配的会话中全局严格连续序列号
    private String sessionId; // 会话id
    private int status; //  消息状态 1:未读   2:已读
    private int sendStatus; //  消息发送状态 1:发送中 2:已送达 3:发送成功，4:发送失败
    private int retryCount; // 重发次数


    private boolean isLocalTemp; // 是否是本地临时消息
    public Message() {
    }

    public String generateSessionId() {
        return generateSessionId(messageFrom, messageTo);
    }

    public static String generateSessionId(long messageFrom, long messageTo) {
        long min = Math.min(messageFrom, messageTo);
        long max = Math.max(messageFrom, messageTo);
        return min + "-" + max;
    }

    // getters and setters
    public long getMessageId() { return messageId; }
    public void setMessageId(long messageId) { this.messageId = messageId; }
    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public long getMessageTo() { return messageTo; }
    public void setMessageTo(long messageTo) { this.messageTo = messageTo; }
    public long getMessageFrom() { return messageFrom; }
    public void setMessageFrom(long messageFrom) { this.messageFrom = messageFrom; }
    public int getMessageType() { return messageType; }
    public void setMessageType(int messageType) { this.messageType = messageType; }
    public long getClientSendTime() { return clientSendTime; }
    public void setClientSendTime(long clientSendTime) { this.clientSendTime = clientSendTime; }
    public int getSendStatus() {
        return sendStatus;
    }
    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }
    public String getMessageToName() {
        return messageToName;
    }
    public void setMessageToName(String messageToName) {
        this.messageToName = messageToName;
    }
    public String getMessageFromName() {
        return messageFromName;
    }
    public void setMessageFromName(String messageFromName) {
        this.messageFromName = messageFromName;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public boolean isLocalTemp() {
        return isLocalTemp;
    }

    public void setLocalTemp(boolean localTemp) {
        isLocalTemp = localTemp;
    }

    public long getClientSeq() {
        return clientSeq;
    }

    public void setClientSeq(long clientSeq) {
        this.clientSeq = clientSeq;
    }

    public long getClientMsgId() {
        return clientMsgId;
    }

    public void setClientMsgId(long clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}

