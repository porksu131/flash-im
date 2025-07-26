package com.szr.flashim.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.szr.flashim.general.constant.OnlineStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Friend {
    private long uid;
    private long friendId;
    private String friendName;
    private int unReadCount = 0;
    private Integer onlineStatus = OnlineStatus.OFFLINE;  // 在线状态
    private Long lastActiveTime; // 最后活跃时间

    public Friend() {
    }

    public Friend(long uid, long friendId, String friendName) {
        this.uid = uid;
        this.friendId = friendId;
        this.friendName = friendName;
    }

    // getters and setters
    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public int getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        this.unReadCount = unReadCount;
    }

    public Integer getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public boolean isOnline() {
        return onlineStatus != null && onlineStatus == OnlineStatus.ONLINE;
    }
}
