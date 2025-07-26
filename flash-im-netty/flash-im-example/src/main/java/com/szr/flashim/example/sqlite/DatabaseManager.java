package com.szr.flashim.example.sqlite;

import com.szr.flashim.example.model.Message;
import com.szr.flashim.example.model.UserInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseManager {
    private static final String APP_NAME = "FlashIm";
    private static final String DB_BASE_URL = "jdbc:sqlite:" + getAppDataDir();

    private static String getAppDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path path;

        if (os.contains("win")) {
            // Windows: AppData/Roaming
            path = Paths.get(System.getenv("APPDATA"));
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support
            path = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
        } else {
            // Linux/Unix: ~/.config
            path = Paths.get(System.getProperty("user.home"), ".config");
        }

        // 创建应用专属目录
        Path appDir = path.resolve(APP_NAME);
        try {
            Files.createDirectories(appDir);
        } catch (IOException e) {
            System.err.println("创建应用目录时出错: " + e.getMessage());
        }
        return appDir.toString() + File.separator;
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.err.println("加载sqlite连接驱动时出错: " + e.getMessage());
        }
    }

    private UserInfo currentUser;

    private Connection connection;

    public DatabaseManager(UserInfo currentUser) {
        this.currentUser = currentUser;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            String DB_URL = DB_BASE_URL + "flash_im_" + currentUser.getUid() + ".db";
            connection = DriverManager.getConnection(DB_URL);
            enableWAL();
            createTables();
        } catch (SQLException e) {
            System.out.println("DatabaseManager 初始化失败：" + e.getMessage());
        }
    }

    public void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS messages (" +
                "sequence_id INTEGER, " +
                "message_id INTEGER, " +
                "message_content TEXT NOT NULL, " +
                "message_to INTEGER NOT NULL, " +
                "message_to_name TEXT, " +
                "message_from INTEGER NOT NULL, " +
                "message_from_name TEXT, " +
                "message_type INTEGER NOT NULL, " +
                "client_send_time INTEGER NOT NULL, " +
                "status INTEGER NOT NULL, " +
                "send_status INTEGER NOT NULL," +
                "client_msg_id INTEGER PRIMARY KEY NOT NULL," +
                "client_seq INTEGER NOT NULL," +
                "session_id TEXT," +
                "is_local_temp INTEGER NOT NULL," +
                "retry_count INTEGER NOT NULL " +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);

            // 创建索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_message_to_message_from ON messages (message_to, message_from);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_seq ON messages(sequence_id);");
        } catch (SQLException e) {
            System.err.println("创建表时出错: " + e.getMessage());
        }
    }

    public synchronized void saveMessage(Message message) {
        String sql = "INSERT OR REPLACE INTO messages VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, message.getSequenceId());
            pstmt.setLong(2, message.getMessageId());
            pstmt.setString(3, message.getMessageContent());
            pstmt.setLong(4, message.getMessageTo());
            pstmt.setString(5, message.getMessageToName());
            pstmt.setLong(6, message.getMessageFrom());
            pstmt.setString(7, message.getMessageFromName());
            pstmt.setInt(8, message.getMessageType());
            pstmt.setLong(9, message.getClientSendTime());
            pstmt.setInt(10, message.getStatus());
            pstmt.setInt(11, message.getSendStatus());
            pstmt.setLong(12, message.getClientMsgId());
            pstmt.setLong(13, message.getClientSeq());
            pstmt.setString(14, message.getSessionId());
            pstmt.setBoolean(15, message.isLocalTemp());
            pstmt.setInt(16, message.getRetryCount());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存消息时出错: " + e.getMessage());
        }
    }

    public void enableWAL() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        } catch (SQLException e) {
            System.err.println("启用WAL出错: " + e.getMessage());
        }
    }

    public synchronized long getMaxSequenceId(String sessionId) {
        String sql = "SELECT MAX(sequence_id) as sequence_id FROM messages where session_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong("sequence_id");
            }

        } catch (SQLException e) {
            System.err.println("获取最大序列号id出错: " + e.getMessage());
        }
        return 0;
    }

    public long getMaxClientSeq() {
        String sql = "SELECT MAX(client_seq) FROM messages";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public synchronized List<Message> loadSendFailMessages(String sessionId) {
        String sql = "SELECT * FROM messages WHERE session_id = ? AND send_status = 3 ";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            return executeMessageQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("加载消息时出错: " + e.getMessage());
        }
        return null;
    }

    public synchronized List<Message> loadMessages(String sessionId, long afterSequenceId, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE session_id = ? AND sequence_id < ? ORDER BY sequence_id DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, sessionId);
            pstmt.setLong(2, afterSequenceId);
            pstmt.setInt(3, limit);

            return executeMessageQuery(pstmt);
        } catch (SQLException e) {
            System.err.println("加载消息时出错: " + e.getMessage());
        }

        // 反转列表保持正序，因为我们按降序加载，所以需要反转，这样旧的消息在前
        Collections.reverse(messages);
        return messages;
    }

    private List<Message> executeMessageQuery(PreparedStatement pstmt) throws SQLException {
        List<Message> messages = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Message msg = new Message();
                msg.setMessageId(rs.getLong("message_id"));
                msg.setMessageContent(rs.getString("message_content"));
                msg.setMessageTo(rs.getLong("message_to"));
                msg.setMessageToName(rs.getString("message_to_name"));
                msg.setMessageFrom(rs.getLong("message_from"));
                msg.setMessageFromName(rs.getString("message_from_name"));
                msg.setMessageType(rs.getInt("message_type"));
                msg.setClientSendTime(rs.getLong("client_send_time"));
                msg.setStatus(rs.getInt("status"));
                msg.setSendStatus(rs.getInt("send_status"));
                msg.setClientMsgId(rs.getLong("client_msg_id"));
                msg.setClientSeq(rs.getLong("client_seq"));
                msg.setSessionId(rs.getString("session_id"));
                msg.setLocalTemp(rs.getBoolean("is_local_temp"));
                msg.setSequenceId(rs.getLong("sequence_id"));
                messages.add(msg);
            }
        }
        return messages;
    }

    public int getUnreadCount(long peerId) {
        String sql = "SELECT COUNT(*) FROM messages "
                + "WHERE message_to = ? AND status = '2'";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setLong(1, currentUser.getUid());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("获取未读消息数时出错: " + e.getMessage());
        }
        return 0;
    }

    // 批量更新状态
    public void batchUpdateStatus(List<Long> messageIds, int newStatus) {
        if (messageIds.isEmpty()) return;

        try {
            StringBuilder sql = new StringBuilder(
                    "UPDATE messages SET status = ? WHERE messageId IN (");

            for (int i = 0; i < messageIds.size(); i++) {
                sql.append("?");
                if (i < messageIds.size() - 1) sql.append(",");
            }
            sql.append(")");

            try (Connection connection = DriverManager.getConnection(DB_BASE_URL);
                 PreparedStatement ps = connection.prepareStatement(sql.toString())) {
                ps.setInt(1, newStatus);
                for (int i = 0; i < messageIds.size(); i++) {
                    ps.setLong(i + 2, messageIds.get(i));
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("批量更新状态时出错: " + e.getMessage());
        }
    }


    public synchronized void deleteMessage(Message message) {
        String sql = "delete from messages where client_msg_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, message.getClientMsgId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除消息时出错: " + e.getMessage());
        }
    }
}