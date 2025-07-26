package com.szr.flashim.auth.model.pojo;

public class FriendRelation {
    private long id; // 主键
    private long uid; // 用户id
    private long friendId; // 好友id
    private String userName; // 用户名称
    private String friendName; //  好友名称

    // getters and setters
    public long getId() {return id;}
    public void setId(long id) {
        this.id = id;
    }
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
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getFriendName() {
        return friendName;
    }
    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }
}
