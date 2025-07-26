package com.szr.flashim.mq.starter;

public enum MqEventType {
    SINGLE_CHAT_BEGIN(1, "发起单聊"),
    SINGLE_CHAT_DISPATCH(2, "转发消息"),
    SINGLE_CHAT_DISPATCH_SUCCESS(3, "消息转发成功"),
    SINGLE_CHAT_DISPATCH_FAIL(4, "消息转发失败"),
    BATCH_MSG_DISPATCH(5, "转发批量消息"),
    BATCH_MSG_DISPATCH_SUCCESS(6, "批量消息转发成功"),
    BATCH_MSG_DISPATCH_FAIL(7, "批量消息转发失败"),
    USER_ONLINE_CHANGE_NOTIFY(8, "用户在线状态变更通知");

    private final int code;
    private final String desc;

    MqEventType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public boolean equals(int code) {
        return this.code == code;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}
