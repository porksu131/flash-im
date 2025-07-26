package com.szr.flashim.example.gui.menu;

import com.szr.flashim.example.model.Message;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PopupMenuLUtil {
    List<PopupMenuItemInfo> popupMenuItemInfoList = new ArrayList<PopupMenuItemInfo>();

    public PopupMenuLUtil() {
    }

    public PopupMenuLUtil(List<PopupMenuItemInfo> popupMenuItemInfoList) {
        this.popupMenuItemInfoList.addAll(popupMenuItemInfoList);
    }

    public JPopupMenu bind(JComponent component, boolean isAddCustomer) {
        /// 1. 创建弹出菜单
        JPopupMenu popupMenu = new JPopupMenu();

        // 2. 创建菜单项
        if (component instanceof JTextComponent) {
            JTextComponent textComponent = (JTextComponent) component;
            JMenuItem copyItem = new JMenuItem("复制");
            JMenuItem pasteItem = new JMenuItem("粘贴");
            JMenuItem cutItem = new JMenuItem("剪切");

            copyItem.addActionListener(e -> textComponent.copy());
            pasteItem.addActionListener(e -> textComponent.paste());
            cutItem.addActionListener(e -> textComponent.cut());

            popupMenu.add(copyItem);
            popupMenu.add(pasteItem);
            popupMenu.add(cutItem);
        }

        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            JMenuItem copyItem = new JMenuItem("复制");
            copyItem.addActionListener(e -> {
                copyToClipboard(label.getText());
            });
            popupMenu.add(copyItem);
        }

        if (component instanceof JList<?>) {
            JList<?> jList = (JList<?>) component;
            JMenuItem copyItem = new JMenuItem("复制");
            copyItem.addActionListener(e -> {
                Object selectedValue = jList.getSelectedValue();
                if (selectedValue != null) {
                    if (selectedValue instanceof Message) {
                        copyToClipboard(((Message) selectedValue).getMessageContent());
                    }
                }
            });
            popupMenu.add(copyItem);
        }

        if (isAddCustomer && !popupMenuItemInfoList.isEmpty()) {
            // 添加菜单项到弹出菜单
            if (popupMenu.getComponents().length > 0) {
                popupMenu.addSeparator(); // 分隔线
            }
            for (PopupMenuItemInfo popupMenuItemInfo : popupMenuItemInfoList) {
                JMenuItem newItem = new JMenuItem(popupMenuItemInfo.getText());
                newItem.addActionListener(popupMenuItemInfo.getActionListener());
                popupMenu.add(newItem);
            }
        }

        // 4. 为组件添加鼠标监听器
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    if (component instanceof JList<?>) {
                        JList<?> jList = (JList<?>) component;
                        // 确定点击位置对应的列表项
                        Point point = e.getPoint();
                        int index = jList.locationToIndex(point);
                        if (index != -1) {
                            // 选中该项
                            jList.setSelectedIndex(index);
                            // 显示菜单
                            popupMenu.show(jList, e.getX(), e.getY());
                        }
                        return;
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        return popupMenu;
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }
}
