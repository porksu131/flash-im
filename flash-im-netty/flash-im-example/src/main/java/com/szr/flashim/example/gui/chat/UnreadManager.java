package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Friend;
import com.szr.flashim.example.sqlite.DatabaseManager;

import javax.swing.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UnreadManager {
    private final Map<Long, AtomicInteger> unreadCounts = new ConcurrentHashMap<>();
    private final DatabaseManager databaseManager;

    public UnreadManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void increment(long friendId) {
        unreadCounts.computeIfAbsent(friendId, k ->
            new AtomicInteger(databaseManager.getUnreadCount(friendId)))
            .incrementAndGet();
    }

    public void reset(long friendId) {
        unreadCounts.put(friendId, new AtomicInteger(0));
    }

    public int getCount(long friendId) {
        return unreadCounts.getOrDefault(friendId, new AtomicInteger()).get();
    }

    public void updateUI(JList<Friend> friendList) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < friendList.getModel().getSize(); i++) {
                Friend f = friendList.getModel().getElementAt(i);
                f.setUnReadCount(getCount(f.getFriendId()));
            }
            friendList.repaint();
        });
    }
}