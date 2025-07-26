package com.szr.flashim.general.model;

import com.google.protobuf.ByteString;
import com.szr.flashim.general.constant.OnlineStatus;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.MsgType;
import com.szr.flashim.general.enumeration.SerialType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.protoc.*;

import java.util.HashMap;

public class ImMessage {
    /**
     * 版本号
     */
    private int version;
    /**
     * 序列化类型，protobuf/json用于序列化和反序列化body
     */
    private int serialType;
    /**
     * 消息ID，唯一id
     */
    private long msgId;
    /**
     * 核心业务类型，认证、心跳、单聊、群聊、红包等
     */
    private int bizType;
    /**
     * 核心业务下的不同请求
     */
    private int subBizType;
    /**
     * 消息类型，请求、响应、通知等
     */
    private int msgType;
    /**
     * 扩展字段
     */
    private HashMap<String, String> extraFields;
    /**
     * 消息体
     */
    private byte[] body;

    public ImMessage() {
    }


    public static ImMessage createMessageResponse(ImMessage request, int code, String msg, byte[] body) {
        CommonResponse chatResponse = CommonResponse.newBuilder()
                .setCode(code)
                .setMsg(msg)
                .setData(ByteString.copyFrom(body))
                .build();
        return createMessageResponse(request, code, msg, chatResponse);
    }

    public static ImMessage createMessageResponse(ImMessage request, int code, String msg) {
        CommonResponse chatResponse = CommonResponse.newBuilder()
                .setCode(code)
                .setMsg(msg)
                .build();
        return createMessageResponse(request, code, msg, chatResponse);
    }

    public static ImMessage createMessageResponse(ImMessage request, int code, String msg, CommonResponse chatResponse) {
        ImMessage imMessage = buildDefaultImMessage();
        imMessage.setMsgId(request.getMsgId());
        imMessage.setMsgType(MsgType.RESPONSE.getCode());
        imMessage.setBizType(request.getBizType());
        imMessage.setSerialType(request.getSerialType());
        byte[] body = chatResponse.toByteArray();
        imMessage.setBody(body);
        return imMessage;
    }

    public static ImMessage createHeartBeat(long msgId, HeartBeat heartBeat) {
        ImMessage imMessage = buildDefaultImMessage();
        imMessage.setMsgId(0L);
        imMessage.setBizType(BizType.HEART_BEAT.getCode());
        imMessage.setSubBizType(-1);
        imMessage.setMsgType(MsgType.REQUEST.getCode());
        imMessage.setBody(heartBeat.toByteArray());
        return imMessage;
    }

    public static ImMessage createLoginMessage(long msgId, AuthMessage request) {
        return createImMessageRequest(msgId, BizType.AUTH, SubBizType.LOGIN, request.toByteArray());
    }


    public static ImMessage createLogoutMessage(long msgId, AuthMessage request) {
        return createImMessageRequest(msgId, BizType.AUTH, SubBizType.LOGOUT, request.toByteArray());
    }

    public static ImMessage createSingleChatMessage(long msgId, ChatMessage request) {
        return createImMessageRequest(msgId, BizType.SINGLE_CHAT, SubBizType.SINGLE_MSG, request.toByteArray());
    }

    public static ImMessage createBatchMessage(long msgId, BatchMessage request) {
        return createImMessageRequest(msgId, BizType.SINGLE_CHAT, SubBizType.BATCH_MSG, request.toByteArray());
    }

    private static ImMessage createImMessageRequest(long msgId, BizType bizType, SubBizType subBizType, byte[] byteArray) {
        ImMessage imMessage = buildDefaultImMessage();
        imMessage.setMsgId(msgId);
        imMessage.setMsgType(MsgType.REQUEST.getCode());
        imMessage.setBizType(bizType.getCode());
        imMessage.setSubBizType(subBizType.getCode());
        imMessage.setBody(byteArray);
        return imMessage;
    }

    public static ImMessage createOfflineNotify(long msgId, FriendNotify request) {
        SubBizType subBizType = OnlineStatus.ONLINE == request.getOperationType() ? SubBizType.LOGIN : SubBizType.LOGOUT;
        return createImMessageAuNotify(msgId, subBizType, request.toByteArray());
    }

    public static ImMessage createOnlineNotify(long msgId, AuthNotify request) {
        return createImMessageAuNotify(msgId, SubBizType.LOGIN, request.toByteArray());
    }

    public static ImMessage createOfflineNotify(long msgId, AuthMessage request) {
        return createImMessageAuNotify(msgId, SubBizType.LOGOUT, request.toByteArray());
    }

    private static ImMessage createImMessageAuNotify(long msgId, SubBizType subBizType, byte[] byteArray) {
        ImMessage imMessage = buildDefaultImMessage();
        imMessage.setMsgId(msgId);
        imMessage.setMsgType(MsgType.NOTIFY.getCode());
        imMessage.setBizType(BizType.AUTH.getCode());
        imMessage.setSubBizType(subBizType.getCode());
        imMessage.setBody(byteArray);
        return imMessage;
    }

    public static ImMessage buildDefaultImMessage() {
        ImMessage imMessage = new ImMessage();
        imMessage.setVersion(1);
        imMessage.setSerialType(2);
        return imMessage;
    }


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getMsgType() {
        return msgType;
    }

    public MsgType getMsgTypeEnum() {
        return MsgType.getMsgType(msgType);
    }

    public SerialType getSerialTypeEnum() {
        return SerialType.getSerialType(serialType);
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getSerialType() {
        return serialType;
    }

    public void setSerialType(int serialType) {
        this.serialType = serialType;
    }

    public int getBizType() {
        return bizType;
    }

    public int getSubBizType() {
        return subBizType;
    }

    public void setSubBizType(int subBizType) {
        this.subBizType = subBizType;
    }

    public BizType getBizTypeEnum() {
        return BizType.getBizType(bizType);
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public HashMap<String, String> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(HashMap<String, String> extraFields) {
        this.extraFields = extraFields;
    }
}
