package com.szr.flashim.mq.starter;

import java.time.LocalDateTime;

public class RocketMqMessage {
    private String id;
    private int mqEventType;
    private byte[] content;
    private LocalDateTime timestamp;


    public int getMqEventType() {
        return mqEventType;
    }

    public void setMqEventType(int mqEventType) {
        this.mqEventType = mqEventType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
