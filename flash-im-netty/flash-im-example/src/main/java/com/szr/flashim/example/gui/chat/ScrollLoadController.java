package com.szr.flashim.example.gui.chat;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ScrollLoadController implements AdjustmentListener {
    private final JScrollPane scrollPane;
    private int lastValue = -1;
    private boolean isLoading = false;
    private final ChatPanel chatPanel;

    public ScrollLoadController(JScrollPane scrollPane, ChatPanel chatPanel) {
        this.scrollPane = scrollPane;
        this.chatPanel = chatPanel;
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        int currentValue = bar.getValue();
        
        // 检查是否滚动到顶部
        if (currentValue == 0 && lastValue > 0 && !isLoading) {
            isLoading = true;
            
            // 使用SwingWorker避免阻塞UI
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    chatPanel.loadHistoryData();
                    return null;
                }
                
                @Override
                protected void done() {
                    isLoading = false;
                }
            }.execute();
        }
        
        lastValue = currentValue;
        
        // 当滚动到底部时，标记消息为已读
        if (bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 20) {
            //model.markAsRead();
            // 标记消息未已读，发送已读回执
        }
    }
}