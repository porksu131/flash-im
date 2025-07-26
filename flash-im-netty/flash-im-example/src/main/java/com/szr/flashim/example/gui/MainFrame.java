package com.szr.flashim.example.gui;

import com.szr.flashim.core.netty.async.NettyClientSendCallBack;
import com.szr.flashim.core.netty.processor.NettyReceiveAuthNotifyInvoke;
import com.szr.flashim.core.netty.processor.NettyReceiveChatMessageInvoke;
import com.szr.flashim.example.gui.chat.ChatPanel;
import com.szr.flashim.example.gui.chat.UnreadManager;
import com.szr.flashim.example.gui.friend.FriendJList;
import com.szr.flashim.example.gui.friend.FriendSearchDialog;
import com.szr.flashim.example.gui.menu.PopupMenuItemInfo;
import com.szr.flashim.example.gui.menu.PopupMenuLUtil;
import com.szr.flashim.example.model.Friend;
import com.szr.flashim.example.model.Message;
import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.example.nettyclient.ExampleNettyClientManager;
import com.szr.flashim.example.nettyclient.HousekeepingChannelListener;
import com.szr.flashim.example.nettyclient.ReconnectManager;
import com.szr.flashim.example.service.ApiService;
import com.szr.flashim.example.sqlite.DatabaseManager;
import com.szr.flashim.general.constant.MessageType;
import com.szr.flashim.general.constant.OnlineStatus;
import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.general.model.protoc.ChatMessage;
import com.szr.flashim.general.model.protoc.CommonResponse;
import com.szr.flashim.general.model.protoc.FriendNotify;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class MainFrame extends JFrame {
    private final ScheduledExecutorService schedulerExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private final UserInfo currentUser;
    private String currentServer = "";
    private JLabel statusLabel;
    private FriendJList friendList;
    private JPanel chatWrapperPanel;
    private ChatPanel currentChatPanel;
    private final Map<String, ChatPanel> chatPanels = new HashMap<>();
    private final ExampleNettyClientManager nettyClientManager;
    private final HousekeepingChannelListener housekeepingChannelListener;
    private final SnowflakeIdGenerator idGenerator;
    private PopupMenuLUtil popupMenuLUtil;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final UnreadManager unreadManager;
    private final DatabaseManager databaseManager;

    public MainFrame(UserInfo user) throws Exception {
        this.currentUser = user;
        this.currentServer = ApiService.GATEWAY_TCP_SERVER;
        housekeepingChannelListener = new HousekeepingChannelListener(new ReconnectManager(this));
        NettyReceiveChatMessageInvoke receiveChatMessageInvoke = new ReceiveChatMessageInvokeImpl();
        NettyReceiveAuthNotifyInvoke receiveAuthNotifyInvoke = new ReceiveNotifyMessageInvokeImpl();
        this.nettyClientManager = new ExampleNettyClientManager(this.currentServer, receiveChatMessageInvoke, receiveAuthNotifyInvoke, housekeepingChannelListener);
        this.idGenerator = new SnowflakeIdGenerator(new Random().nextLong(0, 1023));
        this.databaseManager = new DatabaseManager(currentUser);
        unreadManager  = new UnreadManager(databaseManager);
        initPopupMenuUtil();
        setupUI();
        loadData();
    }

    // 初始化右键菜单
    private void initPopupMenuUtil() {
        List<PopupMenuItemInfo> popupMenuItemInfoList = new ArrayList<>();
        PopupMenuItemInfo popupMenuItemInfo1 = new PopupMenuItemInfo();
        popupMenuItemInfo1.setText("刷新好友列表");
        popupMenuItemInfo1.setActionListener(e -> friendList.loadFriendList());
        popupMenuItemInfoList.add(popupMenuItemInfo1);
        PopupMenuItemInfo popupMenuItemInfo2 = new PopupMenuItemInfo();
        popupMenuItemInfo2.setText("拉取最新消息");
        popupMenuItemInfo2.setActionListener(e -> loadLatestMessage());
        popupMenuItemInfoList.add(popupMenuItemInfo2);
        this.popupMenuLUtil = new PopupMenuLUtil(popupMenuItemInfoList);
    }

    // 主界面
    private void setupUI() {
        setTitle("  聊天客户端");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        URL imageURL = MainFrame.class.getClassLoader().getResource("image/chat-logo.png");
        if (imageURL != null) {
            setIconImage(new ImageIcon(imageURL).getImage());
        }

        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240, 242, 245));
        topPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 8));
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(new Color(240, 242, 245));
        JLabel userLabel = new JLabel(currentUser.getUserName() + " [" + currentUser.getUid() + "] ");
        popupMenuLUtil.bind(userLabel, false);
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        userPanel.add(userLabel);
        topPanel.add(userPanel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("切换用户");
        logoutBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logoutBtn.setBackground(new Color(0, 150, 136));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());
        topPanel.add(logoutBtn, BorderLayout.EAST);

        // 服务连接面板
        JPanel serverWrapperPanel = new JPanel(new BorderLayout());
        serverWrapperPanel.setBackground(new Color(240, 242, 245));
        serverWrapperPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 8));
        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverPanel.setBackground(new Color(240, 242, 245));

        JLabel serviceAddrLabel = new JLabel("服务地址:[" + currentServer + "]");
        serviceAddrLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        popupMenuLUtil.bind(serviceAddrLabel, false);
        serverPanel.add(serviceAddrLabel);
        statusLabel = new JLabel("未连接");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        serverPanel.add(statusLabel);

        // 添加好友按钮
        JButton addButton = new JButton("添加好友");
        addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        addButton.setBackground(new Color(0, 150, 136));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> showAddFriendDialog());

        serverWrapperPanel.add(serverPanel, BorderLayout.WEST);
        serverWrapperPanel.add(addButton, BorderLayout.EAST);


        // 主内容区
        JSplitPane contentPanel = new JSplitPane();
        contentPanel.setDividerLocation(180);
        contentPanel.setDividerSize(0);
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.setBackground(new Color(240, 242, 245));

        JScrollPane friendScrollPanel = new JScrollPane();
        friendList = new FriendJList(currentUser);
        friendList.addListSelectionListener(e -> showChatWindow());
        friendScrollPanel.setViewportView(friendList);

        friendScrollPanel.setBorder(BorderFactory.createTitledBorder("联系人列表"));
        friendScrollPanel.setPreferredSize(new Dimension(100, 0));
        friendScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        chatWrapperPanel = new JPanel(new BorderLayout());
        chatWrapperPanel.setBorder(BorderFactory.createTitledBorder("提示：请选择一个联系人..."));
        chatWrapperPanel.setBackground(new Color(240, 242, 245));

        contentPanel.setLeftComponent(friendScrollPanel);
        contentPanel.setRightComponent(chatWrapperPanel);

        // 主面板使用GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 242, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(topPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(serverWrapperPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        mainPanel.add(contentPanel, gbc);

        add(mainPanel);
    }

    // 打开添加好友窗口
    private void showAddFriendDialog() {
        FriendSearchDialog friendSearchDialog = new FriendSearchDialog(this, friendList, currentUser);
        friendSearchDialog.setVisible(true);
    }

    private void loadData() throws Exception {
        executorService.execute(() -> {
            // 自动连接服务器
            connectServer();

            // 加载好友列表
            friendList.loadFriendList();


//            // 添加未读计数更新定时器
//            new Timer(5000, e -> {
//                unreadManager.updateUI(friendList);
//            }).start();
        });
    }


    private void loadLatestMessage() {
        // todo
        Long userId = currentUser.getUid();

    }


    public void connectServer() {
        try {
            connectNettyServer(currentUser.getUid(), currentUser.getAccessToken());
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("连接失败:" + e.getMessage());
                statusLabel.setForeground(Color.RED);
            });
        }
    }

    public void updateConnectStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(Color.ORANGE);
        });
    }

    // 认证成功才建立连接（网关层本地保存连接信息，redis保存连接信息）
    private void connectNettyServer(long uid, String token) throws Exception {
        this.nettyClientManager.connectAsync(uid, token, new NettyClientSendCallBack() {
            @Override
            public void onSendSuccess(CommonResponse response) {
                boolean connected = response.getCode() == ResponseCode.SUCCESS;
                statusLabel.setText(connected ? "连接成功" : "连接失败:" + response.getMsg());
                statusLabel.setForeground(connected ? new Color(0, 150, 136) : Color.RED);
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                    scheduledFuture = null;
                }
                scheduledFuture = schedulerExecutor.scheduleAtFixedRate(() -> {
                    sendHeartBeat();
                }, 10, 20, TimeUnit.SECONDS);
            }

            @Override
            public void onSendFail(Throwable throwable) {
                statusLabel.setText("连接失败:" + throwable.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        });
    }

    public void sendHeartBeat() {
        try {
            this.nettyClientManager.sendHeartBeatMessageAsync(currentUser.getUid(), new NettyClientSendCallBack() {

                @Override
                public void onSendSuccess(CommonResponse response) {
                    statusLabel.setText("连接成功");
                    statusLabel.setForeground(new Color(0, 150, 136));
                    friendList.updateOnlineStatus(currentUser.getUid(), OnlineStatus.ONLINE);
                }

                @Override
                public void onSendFail(Throwable throwable) {
                    statusLabel.setText("连接异常:" + throwable.getMessage());
                    statusLabel.setForeground(Color.RED);
                    friendList.updateOnlineStatus(currentUser.getUid(), OnlineStatus.OFFLINE);
                }
            });
        } catch (Exception e) {
            System.out.println("心跳发送异常:" + e.getMessage());
        }
    }

    private void showChatWindow() {
        Friend friend = friendList.getSelectedValue();
        if (friend != null) {
            String sessionId = Message.generateSessionId(currentUser.getUid(), friend.getFriendId());
            if (currentChatPanel != null && currentChatPanel.sessionIdEquals(sessionId)) {
                return; // 已经打开会话窗口
            }
            chatWrapperPanel.setBorder(BorderFactory.createTitledBorder("聊天窗口：" + friend.getFriendName()));
            if (currentChatPanel != null) {
                chatWrapperPanel.remove(currentChatPanel); // 先移除
            }
            if (!chatPanels.containsKey(sessionId)) {
                ChatPanel chatPanel = new ChatPanel(currentUser, friend, idGenerator, nettyClientManager, databaseManager);
                currentChatPanel = chatPanel;
                chatPanels.put(sessionId, chatPanel);
            } else {
                currentChatPanel = chatPanels.get(sessionId);
            }
            chatWrapperPanel.add(currentChatPanel, BorderLayout.CENTER);// 在添加
            chatWrapperPanel.revalidate();
            chatWrapperPanel.repaint();

            friend.setUnReadCount(0);
        }
    }


    // 从服务端收到聊天消息
    private void receiveTxtMessageFromServer(Message message) {
        long friendId = message.getMessageFrom();
        if (friendId == currentUser.getUid() && message.getMessageTo() == currentUser.getUid()) {
            return; // 自己收到自己的消息，无需再次加载，因为在发送的时候已经加载过一次了，不然会数据重复；或者后端保存后，不再投递这种属于自己发自己的消息回来
        }

        // 保存到本地数据库
        databaseManager.saveMessage(message);

        String sessionId = message.getSessionId();

        ChatPanel chatPanel = chatPanels.get(sessionId);
        if (chatPanel != null) {
            chatPanel.receiveMessage(message);
        }
        // 如果正在对话中，则直接显示
        if (currentChatPanel != null && currentChatPanel.sessionIdEquals(sessionId)) {
            friendList.zeroUnReadCount(friendId);
        } else {
            // 增加未读消息数量
            friendList.incrementUnRead(friendId);
        }
    }

    // 从服务端收到好友上线和离线提醒
    private void receiveFriendNotifyFromServer(FriendNotify friendNotify) {
        friendList.receiveFriendNotify(friendNotify);
    }


    private void logout() {
        try {
            this.disConnectServer(this.currentServer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(MainFrame.this, "登出异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disConnectServer(String server) {
        try {
            this.nettyClientManager.disConnectAsync(currentUser.getUid(), currentUser.getAccessToken(), new NettyClientSendCallBack() {
                @Override
                public void onSendSuccess(CommonResponse response) {
                    if (response.getCode() == ResponseCode.SUCCESS) {
                        logoutAuthCenter();
                    }
                }

                @Override
                public void onSendFail(Throwable throwable) {
                    statusLabel.setText("登出失败:" + throwable.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            });
        } catch (Exception e) {
            statusLabel.setText("退出连接失败:" + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void logoutAuthCenter() {
        ResponseResult<Void> logout = ApiService.logout(currentUser.getAccessToken());
        if (ResponseResult.isSuccess(logout)) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public void dispose() {
        executorService.execute(() -> {
            this.nettyClientManager.shutdown();
            this.housekeepingChannelListener.shutdown();
        });

        super.dispose();
    }


    // 收到聊天消息时的处理实现
    private class ReceiveChatMessageInvokeImpl implements NettyReceiveChatMessageInvoke {
        @Override
        public void onReceiveMessage(List<ChatMessage> chatMessages) {
            if (CollectionUtils.isEmpty(chatMessages)) {
                return;
            }
            for (ChatMessage chatMessage : chatMessages) {
                if (chatMessage.getMessageType() == MessageType.TEXT) {
                    Message message = new Message();
                    message.setMessageId(chatMessage.getMessageId());
                    message.setMessageContent(new String(chatMessage.getMessageContent().toByteArray()));
                    message.setMessageFrom(chatMessage.getMessageFrom());
                    message.setMessageTo(chatMessage.getMessageTo());
                    message.setMessageType(chatMessage.getMessageType());
                    message.setClientSendTime(chatMessage.getClientSendTime());
                    message.setMessageFromName(chatMessage.getMessageFromName());
                    message.setMessageToName(chatMessage.getMessageToName());
                    message.setStatus(chatMessage.getStatus());
                    message.setClientSeq(chatMessage.getClientSeq());
                    message.setSequenceId(chatMessage.getSequenceId());
                    message.setClientMsgId(chatMessage.getClientMsgId());
                    message.setSessionId(chatMessage.getSessionId());
                    message.setLocalTemp(false);
                    SwingUtilities.invokeLater(() -> {
                        receiveTxtMessageFromServer(message);
                    });

                }
            }
        }
    }

    // 收到提醒消息
    private class ReceiveNotifyMessageInvokeImpl implements NettyReceiveAuthNotifyInvoke {
        @Override
        public void onReceiveNotify(ChannelHandlerContext ctx, ImMessage imMessage) {
            try {
                FriendNotify friendNotify = FriendNotify.parseFrom(imMessage.getBody());
                SwingUtilities.invokeLater(() -> {
                    receiveFriendNotifyFromServer(friendNotify);
                });
            } catch (Exception e) {
                System.out.println("好友上线离线提醒处理失败：:" + e.getMessage());
            }
        }
    }

}

