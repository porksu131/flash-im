package com.szr.flashim.example.gui.chat;

import com.szr.flashim.example.model.Friend;
import com.szr.flashim.example.model.Message;
import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.example.nettyclient.ExampleNettyClientManager;
import com.szr.flashim.example.sqlite.DatabaseManager;
import com.szr.flashim.general.constant.MessageStatus;
import com.szr.flashim.general.constant.MessageType;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ChatPanel extends JPanel {
    private final UserInfo currentUser;
    private final JTextField messageField;
    private final MessageListModel listModel;
    private final JList<Message> jMessageList;
    private final Friend friend;
    private final SnowflakeIdGenerator idGenerator;
    private final String sessionId;

    private final MessageSender messageSender;
    private final MessageReceiver messageReceiver;
    private final DatabaseManager databaseManager;
    private final int LOAD_SIZE = 20;
    private boolean hasMore = true;

    public ChatPanel(UserInfo currentUser,
                     Friend friend,
                     SnowflakeIdGenerator idGenerator,
                     ExampleNettyClientManager nettyClientManager,
                     DatabaseManager databaseManager) {
        super(new BorderLayout());
        this.currentUser = currentUser;
        this.friend = friend;
        this.idGenerator = idGenerator;
        this.databaseManager = databaseManager;
        this.sessionId = Message.generateSessionId(currentUser.getUid(), friend.getFriendId());

        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(255, 255, 255, 225));

        ChatBubbleRenderer chatBubbleRenderer = new ChatBubbleRenderer(currentUser);
        listModel = new MessageListModel();
        jMessageList = new JList<>(listModel);
        jMessageList.setFixedCellWidth(100);
        jMessageList.setCellRenderer(chatBubbleRenderer);
        jMessageList.setBackground(new Color(255, 255, 255, 225));

        // 右键菜单复制
        ChatMessagePopupMenu chatMessagePopupMenu = new ChatMessagePopupMenu(this);
        chatMessagePopupMenu.bind(jMessageList);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(jMessageList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // 确保没有横向滚动条

        // 添加滚动加载监听器
        ScrollLoadController scrollLoadController = new ScrollLoadController(scrollPane, this);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(scrollLoadController);

        // 初始滚动到底部
        scrollerToBottom();


        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        inputPanel.add(messageField, BorderLayout.CENTER);
        JButton sendBtn = new JButton("发送");
        sendBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sendBtn.setBackground(new Color(0, 150, 136));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.addActionListener(e -> sendMessage());
        inputPanel.add(sendBtn, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);


        this.messageSender = new MessageSender(nettyClientManager, databaseManager, listModel);
        this.messageSender.setStatusUpdater(listModel::messageStatusChanged); // 当listModel内的消息状态发生变化时，触发
        this.messageReceiver = new MessageReceiver(listModel);

        loadSessionData();

        // 消息列表点击事件（用于重发单条消息）
        jMessageList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 双击重发
                    int index = jMessageList.locationToIndex(e.getPoint());
                    Message msg = listModel.getElementAt(index);
                    boolean isMeSend = msg.getMessageFrom() == currentUser.getUid();
                    if (isMeSend && msg.getSendStatus() == Message.SEND_STATUS_FAILURE) {
                        resendMessage(msg);
                    }
                }
            }
        });


    }

    // 发送消息
    private void sendMessage() {
        String textMessage = messageField.getText();
        if (!textMessage.isEmpty()) {
            Message sendMsg = buildMessage(textMessage, friend);

            // 串行发送，发送一条得到ack确认后，才接着发送下一条
            messageSender.sendMessage(sendMsg);

            scrollerToBottom();

            clearMessageInput();
        }
        messageField.requestFocus();
    }

    // 接收消息
    public void receiveMessage(Message message) {
        messageReceiver.handleReceivedMessage(message);
        scrollerToBottom();
        //messageField.requestFocus();
    }

    // 删除消息
    public void deleteMessage(Message message) {
        databaseManager.deleteMessage(message);
        listModel.deleteMessage(message);
    }

    // 首次打开窗口时，加载历史数据
    private void loadSessionData() {
        // 从数据库加载当前会话数据
        long maxClientSeq = databaseManager.getMaxClientSeq();
        messageSender.initializeClientSeq(maxClientSeq);

        // 加载最近20条消息
        List<Message> recentMessages = databaseManager.loadMessages(sessionId, Long.MAX_VALUE, LOAD_SIZE);
        if (recentMessages.size() < LOAD_SIZE) {
            hasMore = false;
        }
        listModel.addMessages(recentMessages);
    }

    // 滚动到顶部时，加载历史数据
    public void loadHistoryData() {
        if (hasMore) {
            // 从数据库加载当前会话数据
            long sequenceId = listModel.getOldestSequenceId();
            List<Message> historyMessages = databaseManager.loadMessages(sessionId, sequenceId, LOAD_SIZE);
            // 往前加载历史消息
            listModel.addMessagesToTop(historyMessages);
        }
    }

    // 滚动到底部
    private void scrollerToBottom() {
        SwingUtilities.invokeLater(() -> {
            if (listModel.getSize() > 0) {
                jMessageList.ensureIndexIsVisible(listModel.getSize() - 1);
            }
        });
    }

    // 单条重发
    public void resendMessage(Message failedMessage) {
        messageSender.resendMessage(failedMessage);
    }

    // 批量重发，（可选）
    public void resendAllFailedMessages() {
        List<Message> failedMessages = databaseManager.loadSendFailMessages(sessionId);
        messageSender.resendAllFailedMessages(failedMessages);
    }

    private Message buildMessage(String textMessage, Friend friend) {
        Message sendMsg = new Message();
        sendMsg.setMessageId(-1);
        sendMsg.setSequenceId(-1);
        sendMsg.setMessageContent(textMessage);
        sendMsg.setMessageTo(friend.getFriendId());
        sendMsg.setMessageToName(friend.getFriendName());
        sendMsg.setMessageFrom(currentUser.getUid());
        sendMsg.setMessageFromName(currentUser.getUserName());
        sendMsg.setMessageType(MessageType.TEXT);
        sendMsg.setClientSendTime(System.currentTimeMillis());
        sendMsg.setStatus(MessageStatus.ALREADY_SEND);
        sendMsg.setClientMsgId(idGenerator.nextId());
        sendMsg.setSendStatus(Message.SEND_STATUS_SENDING);
        sendMsg.setLocalTemp(true);
        sendMsg.setSessionId(sendMsg.generateSessionId());
        return sendMsg;
    }


    public boolean sessionIdEquals(String sessionId) {
        return this.sessionId.equals(sessionId);
    }

    private void clearMessageInput() {
        this.messageField.setText("");
    }
}
