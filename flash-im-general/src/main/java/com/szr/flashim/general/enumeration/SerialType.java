package com.szr.flashim.general.enumeration;

public enum SerialType {
    NONE(0, "none"),
    JSON(1, "json"),
    PROTOBUF(2, "protobuf");

    private final int code;
    private final String desc;

    SerialType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SerialType getSerialType(int code) {
        for (SerialType serialType : SerialType.values()) {
            if (serialType.equals(code)) {
                return serialType;
            }
        }
        throw new IllegalArgumentException("unknown serial type code: " + code);
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
