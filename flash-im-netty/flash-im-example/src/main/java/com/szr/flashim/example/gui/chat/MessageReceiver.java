package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Message;

public class MessageReceiver {
    private final MessageListModel listModel;

    public MessageReceiver(MessageListModel listModel) {
        this.listModel = listModel;
    }

    public void handleReceivedMessage(Message msg) {
        listModel.addMessage(msg);
    }
}