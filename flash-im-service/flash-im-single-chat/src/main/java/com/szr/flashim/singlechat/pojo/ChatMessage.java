package com.szr.flashim.singlechat.pojo;

public class ChatMessage {
    private long messageId;         // 消息id
    private String messageContent;  // 消息内容
    private long messageTo;         // 消息接收者id
    private String messageToName;   // 消息接收者名称
    private long messageFrom;       // 消息发送者id
    private String messageFromName; // 消息发送者名称
    private int messageType;        // 消息类型 0:文字 1:图片 2:语音
    private String sessionId;       // 会话id
    private long sequenceId;        // 服务端分配的会话消息序列号
    private long clientMsgId;         // 客户端消息id
    private long clientSeq;         // 客户端临时的消息序列号
    private long clientSendTime;  // 客户端发送时间(时间戳)
    private long createTime;        // 消息创建时间(时间戳)
    private long updateTime;        // 消息保存时间(时间戳)
    private long readTime;          // 消息已读时间(时间戳)
    private int status;             // 消息状态 1:未发送 2:已发送，3:已读


    // getters and setters
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public int getMessageType() {
        return messageType;
    }
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    public String getMessageFromName() {
        return messageFromName;
    }
    public void setMessageFromName(String messageFromName) {
        this.messageFromName = messageFromName;
    }
    public long getMessageFrom() {
        return messageFrom;
    }
    public void setMessageFrom(long messageFrom) {
        this.messageFrom = messageFrom;
    }
    public String getMessageToName() {
        return messageToName;
    }
    public void setMessageToName(String messageToName) {
        this.messageToName = messageToName;
    }
    public long getMessageTo() {
        return messageTo;
    }
    public void setMessageTo(long messageTo) {
        this.messageTo = messageTo;
    }
    public String getMessageContent() {
        return messageContent;
    }
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    public long getMessageId() {
        return messageId;
    }
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
    public long getReadTime() {
        return readTime;
    }
    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }
    public long getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public long getSequenceId() {
        return sequenceId;
    }
    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }
    public long getClientSeq() {
        return clientSeq;
    }
    public void setClientSeq(long clientSeq) {
        this.clientSeq = clientSeq;
    }
    public long getClientSendTime() {
        return clientSendTime;
    }
    public void setClientSendTime(long client_send_time) {
        this.clientSendTime = client_send_time;
    }
    public long getClientMsgId() {
        return clientMsgId;
    }
    public void setClientMsgId(long clientMsgId) {
        this.clientMsgId = clientMsgId;
    }

}
