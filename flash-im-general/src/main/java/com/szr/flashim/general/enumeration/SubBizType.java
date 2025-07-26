package com.szr.flashim.general.enumeration;

public enum SubBizType {
    LOGIN(1, "登录连接"),
    LOGOUT(2, "登出连接"),
    SINGLE_MSG(3, "发送单条消息"),
    BATCH_MSG(4, "发送批量");


    private final int code;
    private final String desc;

    SubBizType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubBizType getBizType(int code) {
        for (SubBizType bizType : SubBizType.values()) {
            if (bizType.code == code) {
                return bizType;
            }
        }
        throw new IllegalArgumentException("unknown subBizType code: " + code);
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
