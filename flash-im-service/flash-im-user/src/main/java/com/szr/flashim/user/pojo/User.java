package com.szr.flashim.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    private long uid; // 用户id
    private String userName; // 用户名
    @JsonIgnore
    private String password; // 密码
    private String phone;


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
