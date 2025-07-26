package com.szr.flashim.example.gui.friend;

import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.example.service.ApiService;
import com.szr.flashim.general.model.ResponseResult;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class FriendSearchDialog extends JDialog {
    private DefaultListModel<UserInfo> searchResultsModel;
    private UserInfo currentUser;
    private FriendJList friendList;

    public FriendSearchDialog(JFrame parent, FriendJList friendList, UserInfo currentUser) {
        super(parent, "添加好友", true);
        this.currentUser = currentUser;
        this.friendList = friendList;

        this.setSize(400, 400);
        this.setLocationRelativeTo(this);
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(Color.WHITE);
        this.setResizable(false);


        this.searchResultsModel = new DefaultListModel<>();

        // 顶部搜索区域
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        searchPanel.setBackground(new Color(240, 242, 245));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String query = searchField.getText().trim();
                    performSearch(query);
                }
            }
        });

        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        searchButton.setBackground(new Color(0, 150, 136));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        this.add(searchPanel, BorderLayout.NORTH);

        // 搜索结果列表
        JList<UserInfo> resultsList = new JList<>(searchResultsModel);
        resultsList.setCellRenderer(new FriendSearchResultRenderer());
        resultsList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultsList.setBackground(new Color(250, 250, 250));
        resultsList.setFixedCellHeight(40);

        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setBorder(new EmptyBorder(0, 15, 15, 15));
        this.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = new JButton("添加");
        addButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        addButton.setBackground(new Color(0, 150, 136));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.setFocusPainted(false);

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setFocusPainted(false);

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // 事件处理
        // 搜索用户按钮
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            performSearch(query);
        });

        // 添加好友按钮
        addButton.addActionListener(e -> {
            int selectedIndex = resultsList.getSelectedIndex();
            if (selectedIndex != -1) {
                UserInfo selectedUser = searchResultsModel.get(selectedIndex);
                if (friendList.existFriend(selectedUser.getUid())) {
                    JOptionPane.showMessageDialog(this, "已经是好友关系无需添加!", "成功", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                try {
                    ResponseResult<Void> addResult = ApiService.addFriendRelation(currentUser.getUid(), selectedUser.getUid(), currentUser.getAccessToken());
                    if (ResponseResult.isSuccess(addResult)) {
                        JOptionPane.showMessageDialog(this, "好友添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                        try {
                            friendList.loadFriendList(); // 添加好友成功后重新加载好友列表
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "好友加载失败:" + ex.getMessage(), "失败", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "好友添加失败:" + ex.getMessage(), "失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(e -> this.dispose());

        // 添加WindowListener来监听窗口打开完成事件
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                performSearch(""); // 默认搜索全部用户
            }
        });
    }

    public void performSearch(String query) {
        ResponseResult<List<UserInfo>> responseResult;
        if (!query.isEmpty()) {

            if (query.matches("\\d+")) { // 数字ID搜索
                responseResult = ApiService.queryByUserId(Long.parseLong(query.trim()), currentUser.getAccessToken());
            } else {
                responseResult = ApiService.queryByUserName(query.trim(), currentUser.getAccessToken());
            }
        } else {
            responseResult = ApiService.queryAllUser();
        }

        if (ResponseResult.isSuccess(responseResult)) {
            if (CollectionUtils.isNotEmpty(responseResult.getData())) {
                searchResultsModel.clear();
                for (UserInfo user : responseResult.getData()) {
                    if (friendList.existFriend(user.getUid())) {
                        continue; // 已经是好友的话，就没必要展示出来了
                    }
                    searchResultsModel.addElement(user);
                }
                if (searchResultsModel.size() <= 0) {
                    JOptionPane.showMessageDialog(this, "抱歉，没有可以用于添加好友的用户", "信息", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "没有查询到任何用户", "信息", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "搜索失败:" + responseResult.getMsg(), "失败", JOptionPane.ERROR_MESSAGE);
        }
    }
}
