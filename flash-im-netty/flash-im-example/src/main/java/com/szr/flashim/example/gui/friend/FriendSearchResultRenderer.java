package com.szr.flashim.example.gui.friend;

import com.szr.flashim.example.model.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FriendSearchResultRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setBorder(new EmptyBorder(1, 15, 1, 15));
        //label.setIcon(new ImageIcon(getClass().getResource("/search_icon.png"))); // 实际使用时替换真实图标
        if (value instanceof UserInfo) {
            UserInfo userInfo = (UserInfo) value;
            label.setText("<html><b>" + userInfo.getUserName() + " </b> [ " + userInfo.getUid() + " ] </html>");
        }
        return label;
    }
}