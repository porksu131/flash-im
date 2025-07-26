package com.szr.flashim.auth.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserInfo {
    private Long uid; // 用户id
    private String userName; // 用户名
    @JsonIgnore
    private String password; // 密码
    private String phone;
    private Boolean enabledFlag;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
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

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
