package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Message;
import com.szr.flashim.example.model.UserInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

// 自定义消息渲染器
public class ChatBubbleRenderer extends JPanel implements ListCellRenderer<Message> {
    private final JPanel contentPanel = new JPanel();
    private final JLabel timeLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel statusIconLabel = new JLabel();
    private final ChatBubble chatBubble = new ChatBubble();
    private final UserInfo currentUser;
    private ImageIcon sendFailIcon;
    private ImageIcon sendingIcon;

    public ChatBubbleRenderer(UserInfo userInfo) {
        this.currentUser = userInfo;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 10, 8, 10));

        URL sendFailURL = getClass().getClassLoader().getResource("image/send-fail.png");
        if (sendFailURL != null) {
            sendFailIcon = new ImageIcon(sendFailURL);
        }

        URL sendingURL = getClass().getClassLoader().getResource("image/sending.png"); // 添加发送中图标
        if (sendingURL != null) {
            sendingIcon = new ImageIcon(sendingURL);
        }

        statusIconLabel.setPreferredSize(new Dimension(25, 25)); // 固定图标大小
        statusIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusIconLabel.setVerticalAlignment(SwingConstants.CENTER);
        //statusIconLabel.setOpaque(false);
        statusIconLabel.setIcon(null);
        statusIconLabel.setVisible(false);

        // 时间标签样式
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setBackground(Color.WHITE);

        // 状态标签样式
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        statusLabel.setForeground(Color.GRAY);

        // 内容标签样式
        //contentPanel.add(statusIconLabel);
        contentPanel.add(statusLabel);
        contentPanel.add(chatBubble);
        contentPanel.setBackground(null);

        JPanel bubblePanel = new JPanel(new BorderLayout());
        bubblePanel.add(timeLabel, BorderLayout.NORTH);
        bubblePanel.add(contentPanel, BorderLayout.CENTER);

        bubblePanel.setBackground(Color.WHITE);
        add(bubblePanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends Message> list,
            Message message,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        // 设置时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 根据消息方向设置样式
        if (message.getMessageFrom() == currentUser.getUid()) {
            // 发送的消息（右侧）
            String timeText = sdf.format(new Date(message.getClientSendTime())) + "  我";
            timeLabel.setText(timeText);
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            chatBubble.setText(message.getMessageContent());
            chatBubble.setRight(true);
            // 设置状态文本
            setStatusLabel(message.getSendStatus());
            setStatusIcon(message.getSendStatus());
            statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contentPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        } else {
            // 接收的消息（左侧）
            timeLabel.setText(message.getMessageFromName() + "  " + sdf.format(new Date(message.getClientSendTime())));
            timeLabel.setHorizontalAlignment(SwingConstants.LEFT);
            setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            chatBubble.setText(message.getMessageContent());
            chatBubble.setRight(false);
            statusLabel.setText("");
            statusLabel.setVisible(false);
            statusIconLabel.setVisible(false);
            contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        }

        return this;
    }

    // 新增：设置状态图标方法
    private void setStatusIcon(int sendStatus) {
        if (sendStatus == Message.SEND_STATUS_FAILURE) {
            statusIconLabel.setVisible(true); // 确保图标可见
            statusIconLabel.setIcon(sendFailIcon);
            statusIconLabel.setToolTipText("发送失败");
        } else if (sendStatus == Message.SEND_STATUS_SENDING) {
            statusIconLabel.setVisible(true); // 确保图标可见
            statusIconLabel.setIcon(sendingIcon);
            statusIconLabel.setToolTipText("发送中");
        } else {
            statusIconLabel.setIcon(null);
            statusIconLabel.setVisible(false);
        }
    }

    private void setStatusLabel(int sendStatus) {
        if (sendStatus == Message.SEND_STATUS_FAILURE) {
            statusLabel.setVisible(true);
            statusLabel.setText("发送失败  ");
            statusLabel.setForeground(Color.RED);
        } else if (sendStatus == Message.SEND_STATUS_SENDING) {
            statusLabel.setVisible(true);
            statusLabel.setText("发送中...  ");
            statusLabel.setForeground(Color.orange);
        } else {
            statusLabel.setText("");
            statusLabel.setForeground(Color.GRAY);
        }
    }

    // 更精准的文本区域检测方法
    public boolean isPointInTextArea(Point pointInList, Rectangle cellBounds) {
        // 转换为渲染器坐标系
        Point pointInRenderer = new Point(
                pointInList.x - cellBounds.x,
                pointInList.y - cellBounds.y
        );

        // 检查是否在时间标签内（排除时间区域）
        if (timeLabel.getBounds().contains(pointInRenderer)) {
            return false;
        }

        // 检查是否在状态标签内（排除时间区域）
        if (statusLabel.getBounds().contains(pointInRenderer)) {
            return false;
        }

        // 检查是否在聊天气泡内
        Point pointInContentPanel = SwingUtilities.convertPoint(
                this, pointInRenderer, contentPanel
        );

        // 获取聊天气泡的实际边界
        Rectangle bubbleBounds = chatBubble.getBounds();
        bubbleBounds.grow(5, 5); // 增加一点容差

        return bubbleBounds.contains(pointInContentPanel);
    }
}

