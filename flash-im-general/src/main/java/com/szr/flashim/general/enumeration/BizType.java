package com.szr.flashim.general.enumeration;

public enum BizType {
    AUTH(1, "认证"),
    SINGLE_CHAT(2, "单聊"),
    GROUP_CHAT(3, "群聊"),
    RED_ENVELOPE(4, "红包"),
    HEART_BEAT(5, "心跳");

    private final int code;
    private final String desc;

    BizType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BizType getBizType(int code) {
        for (BizType bizType : BizType.values()) {
            if (bizType.code == code) {
                return bizType;
            }
        }
        throw new IllegalArgumentException("unknown bizType code: " + code);
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
