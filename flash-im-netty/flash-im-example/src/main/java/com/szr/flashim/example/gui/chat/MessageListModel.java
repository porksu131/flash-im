package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessageListModel extends AbstractListModel<Message> {
    private final List<Message> messages = new ArrayList<>();
    private final Comparator<Message> comparator = (m1, m2) -> {
        // 排序逻辑：优先按服务端序列号，其次按客户端序列号
        if (m1.getSequenceId() != -1 && m2.getSequenceId() != -1) {
            return Long.compare(m1.getSequenceId(), m2.getSequenceId());
        }
        return Long.compare(m1.getClientSeq(), m2.getClientSeq());
    };

    public void addMessage(Message msg) {
        messages.add(msg);
        messages.sort(comparator);
        fireIntervalAdded(this, messages.size() - 1, messages.size() - 1);
    }


    public void deleteMessage(Message msg) {
        int index = messages.indexOf(msg);
        messages.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void addMessages(List<Message> newMessages) {
        if (newMessages.isEmpty()) return;

        int start = messages.size();
        messages.addAll(newMessages);
        messages.sort(comparator);
        fireIntervalAdded(this, start, messages.size() - 1);
    }

    public void addMessagesToTop(List<Message> newMessages) {
        if (newMessages.isEmpty()) return;

        messages.addAll(0, newMessages);
        messages.sort(comparator);
        fireContentsChanged(this, 0, messages.size() - 1);
    }

    public void messageStatusChanged(Message msg) {
        int index = messages.indexOf(msg);
        if (index >= 0) {
            messages.sort(comparator);
            fireContentsChanged(this, index, index);
        }
    }

    public long getOldestSequenceId() {
        return messages.isEmpty() ? 0 : messages.get(0).getSequenceId();
    }

    @Override
    public int getSize() {
        return messages.size();
    }

    @Override
    public Message getElementAt(int index) {
        return messages.get(index);
    }
}