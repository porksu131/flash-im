package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatMessagePopupMenu {
    private final ChatPanel chatPanel;

    public ChatMessagePopupMenu(ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
    }

    public void bind(JList<Message> jList) {
        /// 1. 创建弹出菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("复制");
        copyItem.addActionListener(e -> {
            // 复制文本内容到剪贴板（去除HTML标签）
            Message message = jList.getSelectedValue();
            String plainText = message.getMessageContent().replaceAll("<[^>]*>", "");
            StringSelection selection = new StringSelection(plainText);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        });
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> {
            // 删除消息
            Message message = jList.getSelectedValue();
            chatPanel.deleteMessage(message);
        });
        popupMenu.add(copyItem);
        popupMenu.add(deleteItem);

        jList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }
                // 确定点击位置对应的列表项
                Point point = e.getPoint();
                int index = jList.locationToIndex(point);
                if (index != -1) {
                    // 获取单元格边界
                    Rectangle cellBounds = jList.getCellBounds(index, index);

                    ChatBubbleRenderer renderer = (ChatBubbleRenderer) jList.getCellRenderer();
                    // 获取渲染器组件
                    Component comp = renderer.getListCellRendererComponent(jList, jList.getModel().getElementAt(index),
                            index, false, false);

                    // 确保组件布局正确
                    comp.setBounds(cellBounds);
                    comp.doLayout();

                    // 检查是否点击在文本区域
                    if (renderer.isPointInTextArea(point, cellBounds)) {
                        // 选中改行
                        jList.setSelectedIndex(index);
                        // 显示菜单
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        //renderer.showContextMenu(jList, point, jList.getModel().getElementAt(index));
                    }
                }
            }
        });
    }
}
