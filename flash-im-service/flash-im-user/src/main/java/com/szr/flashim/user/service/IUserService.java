package com.szr.flashim.user.service;

import com.szr.flashim.user.pojo.User;

import java.util.List;

public interface IUserService {
    List<User> queryAllUsers();

    List<User> queryByUserName(String userName);

    List<User> queryByUserId(Long uid);
}
