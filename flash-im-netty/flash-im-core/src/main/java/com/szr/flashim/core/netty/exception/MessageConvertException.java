package com.szr.flashim.core.netty.exception;

public class MessageConvertException extends Exception {
    public MessageConvertException(String message) {
        super(message, null);
    }

    public MessageConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
