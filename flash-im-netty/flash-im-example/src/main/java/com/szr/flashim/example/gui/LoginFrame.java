package com.szr.flashim.example.gui;

import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.example.service.ApiService;
import com.szr.flashim.general.model.ResponseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginFrame extends JFrame {
    private JComboBox<String> userComboBox;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private List<UserInfo> userList = new ArrayList<>();

    public LoginFrame() {
        initUsers();
        setupUI();
    }

    private void initUsers() {
        ResponseResult<List<UserInfo>> listResponseResult = ApiService.queryAllUser();
        if (ResponseResult.isSuccess(listResponseResult) && CollectionUtils.isNotEmpty(listResponseResult.getData())) {
            userList.clear();
            userList.addAll(listResponseResult.getData());
        }
    }

    private void setupUI() {
        URL imageURL = LoginFrame.class.getClassLoader().getResource("image/chat-logo.png");
        if (imageURL != null) {
            setIconImage(new ImageIcon(imageURL).getImage());
        }
        setTitle("聊天客户端登录");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        // mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBackground(new Color(240, 242, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 10, 5);
        gbc.fill = GridBagConstraints.BOTH;


        // 用户下拉列表
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel selectUserLabel = new JLabel("选择用户:");
        selectUserLabel.setPreferredSize(new Dimension(60, 25));
        mainPanel.add(selectUserLabel, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        userComboBox = new JComboBox<>(userList.stream().map(UserInfo::getUserName).toArray(String[]::new));
        userComboBox.setPreferredSize(new Dimension(150, 25));
        userComboBox.addActionListener(e -> updateFields());
        mainPanel.add(userComboBox, gbc);

        // UID
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        mainPanel.add(new JLabel("用户ID:"), gbc);
//        gbc.gridx = 1;
//        gbc.gridy = 1;
//        uidField = new JTextField();
//        uidField.setPreferredSize(new Dimension(150, 25));
//        uidField.setEditable(false);
//        uidField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//        mainPanel.add(uidField, gbc);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(150, 25));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(150, 23));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(passwordField, gbc);

        // 按钮
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 20);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(null);
        JButton loginBtn = new JButton("登录");
        loginBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        loginBtn.setBackground(new Color(0, 150, 136));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.addActionListener(e -> login());
        JButton registerBtn = new JButton("注册");
        registerBtn.setBackground(new Color(255, 99, 71));
        registerBtn.addActionListener(e -> register());
        loginBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        mainPanel.add(buttonPanel, gbc);

        updateFields();
        add(mainPanel);
    }

    private void updateFields() {
        int index = userComboBox.getSelectedIndex();
        if (index >= 0 && index < userList.size()) {
            UserInfo user = userList.get(index);
            // uidField.setText(String.valueOf(user.getUid()));
            usernameField.setText(user.getUserName());
            if (StringUtils.isBlank(user.getPassword())) {
                passwordField.setText("123456"); // 测试期间，懒得输入
            } else {
                passwordField.setText(user.getPassword());
            }
        }
    }

    private void login() {
        try {
            //long uid = Long.parseLong(uidField.getText());
            String password = new String(passwordField.getPassword());
            String userName = usernameField.getText();
            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
                JOptionPane.showMessageDialog(LoginFrame.this, "用户名或密码为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ResponseResult<UserInfo> result = ApiService.login(userName, password);
            if (ResponseResult.isSuccess(result)) {
                new MainFrame(result.getData()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "登录失败:" + result.getMsg(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(LoginFrame.this, "用户ID必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(LoginFrame.this, "登录异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        try {
            String password = new String(passwordField.getPassword());
            String userName = usernameField.getText();
            if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
                JOptionPane.showMessageDialog(LoginFrame.this, "用户名或密码为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ResponseResult<UserInfo> result = ApiService.register(userName, password);
            if (ResponseResult.isSuccess(result)) {
                JOptionPane.showMessageDialog(LoginFrame.this, "注册成功:" + result.getMsg(), "成功", JOptionPane.INFORMATION_MESSAGE);
                this.initUsers();
                userComboBox.setModel(new DefaultComboBoxModel<>(userList.stream().map(UserInfo::getUserName).toArray(String[]::new)));
                userComboBox.setSelectedItem(result.getData().getUserName());
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, "注册失败:" + result.getMsg(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(LoginFrame.this, "用户ID必须是数字", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(LoginFrame.this, "登录异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}



