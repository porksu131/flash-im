package com.szr.flashim.user.service;

import com.szr.flashim.user.mapper.UserMapper;
import com.szr.flashim.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> queryAllUsers() {
        return userMapper.queryAllUsers();
    }

    @Override
    public List<User> queryByUserName(String userName) {
        return userMapper.queryByUserName(userName);
    }

    @Override
    public List<User> queryByUserId(Long uid) {
        User user = userMapper.getByUid(uid);
        if (user != null) {
            return Collections.singletonList(user);
        }
        return null;
    }
}
