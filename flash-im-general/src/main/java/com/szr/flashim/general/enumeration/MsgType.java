package com.szr.flashim.general.enumeration;

public enum MsgType {
    REQUEST(1, "请求"),
    RESPONSE(2, "响应"),
    NOTIFY(3, "通知"),
    HEARTBEAT(4, "心跳");


    private final int code;
    private final String desc;

    MsgType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MsgType getMsgType(int code) {
        for (MsgType msgType : MsgType.values()) {
            if (msgType.code == code) {
                return msgType;
            }
        }
        throw new IllegalArgumentException("unknown msgType code: " + code);
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
