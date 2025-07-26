package com.szr.flashim.example.gui.friend;

import com.szr.flashim.example.model.Friend;
import com.szr.flashim.example.model.UserInfo;
import com.szr.flashim.example.service.ApiService;
import com.szr.flashim.general.constant.OnlineStatus;
import com.szr.flashim.general.enumeration.NotifyType;
import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.general.model.protoc.FriendNotify;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FriendJList extends JList<Friend> {
    private UserInfo currentUser;
    private DefaultListModel<Friend> friendListModel;

    public FriendJList(UserInfo currentUser) {
        super();
        this.currentUser = currentUser;
        friendListModel = new DefaultListModel<>();
        setModel(friendListModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new FriendListRenderer());
        setFixedCellHeight(30);
    }


    public void receiveFriendNotify(FriendNotify friendNotify) {
        long friendId = friendNotify.getUid();
        int onlineStatus = friendNotify.getOperationType() == NotifyType.ON_LINE.getCode() ? OnlineStatus.ONLINE : OnlineStatus.OFFLINE;
        updateOnlineStatus(friendId, onlineStatus);
        sortFriendList();
    }

    public void updateOnlineStatus(long friendId, int onlineStatus) {
        for (int i = 0; i < friendListModel.getSize(); i++) {
            Friend f = friendListModel.getElementAt(i);
            if (f.getFriendId() == friendId) {
                f.setOnlineStatus(onlineStatus);
                repaint();
            }
        }
    }

    public void incrementUnRead(long friendId) {
        for (int i = 0; i < friendListModel.getSize(); i++) {
            Friend f = friendListModel.getElementAt(i);
            if (f.getFriendId() == friendId) {
                f.setUnReadCount(f.getUnReadCount() + 1);
                repaint();
            }
        }
    }

    public void zeroUnReadCount(long friendId) {
        for (int i = 0; i < friendListModel.getSize(); i++) {
            Friend f = friendListModel.getElementAt(i);
            if (f.getFriendId() == friendId) {
                f.setUnReadCount(0);
                repaint();
            }
        }
    }

    public boolean existFriend(long friendId) {
        for (int i = 0; i < friendListModel.getSize(); i++) {
            Friend f = friendListModel.getElementAt(i);
            if (f.getFriendId() == friendId) {
                return true;
            }
        }
        return false;
    }

    public void loadFriendList() {
        ResponseResult<List<Friend>> friendListResult = ApiService.getFriendList(currentUser.getUid(), currentUser.getAccessToken());
        if (ResponseResult.isSuccess(friendListResult)) {
            List<Friend> allFriends = friendListResult.getData();
            if (CollectionUtils.isNotEmpty(allFriends)) {
                Friend me = allFriends.stream().filter(friend -> friend.getFriendId() == currentUser.getUid()).findFirst().orElse(null);
                if (me != null) {
                    me.setFriendName("我");
                    allFriends.remove(me);
                    allFriends.add(0, me);
                }
                if (friendListModel.isEmpty()) {
                    friendListModel.addAll(allFriends);
                } else {
                    List<Friend> newFriends = findNewFriends(allFriends);
                    friendListModel.addAll(newFriends);
                }
            }
        }
        repaint();
    }

    private List<Friend> findNewFriends(List<Friend> friends) {
        List<Friend> newFriends = new ArrayList<>();
        for (Friend friend : friends) {
            if (!existFriend(friend.getFriendId())) {
                newFriends.add(friend);
            }
        }
        return newFriends;
    }


    public void sortFriendList() {
        // 1. 提取模型中的数据到临时列表
        List<Friend> friends = new ArrayList<>();
        for (int i = 0; i < friendListModel.getSize(); i++) {
            friends.add(friendListModel.getElementAt(i));
        }

        // 2. 使用自定义比较器排序
        friends.sort(new Comparator<Friend>() {
            @Override
            public int compare(Friend f1, Friend f2) {
                boolean isMe1 = "我".equals(f1.getFriendName());
                boolean isMe2 = "我".equals(f2.getFriendName());

                if (isMe1 && isMe2) return 0;            // 两个都是"我"
                if (isMe1) return -1;                    // f1是"我"置顶
                if (isMe2) return 1;                     // f2是"我"置顶

                // 在线状态优先：在线排在前（true的"大于"false，返回负值使在线靠前）
                if (f1.isOnline() && !f2.isOnline()) {
                    return -1;  // f1在线，f2离线 -> f1排在f2前
                } else if (!f1.isOnline() && f2.isOnline()) {
                    return 1;   // f1离线，f2在线 -> f1排在f2后
                } else {
                    // 状态相同则按名称排序
                    return f1.getFriendName().compareTo(f2.getFriendName());
                }
            }
        });

        // 3. 清空并重新填充模型
        friendListModel.clear();
        for (Friend friend : friends) {
            friendListModel.addElement(friend);
        }
    }
}
