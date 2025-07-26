package com.szr.flashim.auth.service;

import com.szr.flashim.auth.mapper.UserMapper;
import com.szr.flashim.auth.model.CustomUserDetails;
import com.szr.flashim.auth.model.pojo.FriendRelation;
import com.szr.flashim.auth.model.pojo.UserInfo;
import com.szr.flashim.general.constant.Constants;
import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // security在做密码校验时会从这里查询用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user = userMapper.getByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 应从数据库查询角色，这里简化为固定角色
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("admin"));

        CustomUserDetails customUserDetails = new CustomUserDetails();
        customUserDetails.setUsername(username);
        customUserDetails.setPassword(user.getPassword());
        customUserDetails.setUserId(user.getUid());
        customUserDetails.setAuthorities(authorities);
        return customUserDetails;
    }

    public UserInfo loadUserByUid(Long uid) {
        return userMapper.getByUid(uid);
    }

    public UserInfo saveUser(String userName, String password, String phone) {
        if (userMapper.getByUserName(userName) != null) {
            throw new ServiceException(Constants.FAIL, "用户名已存在");
        }

        UserInfo user = new UserInfo();
        user.setUid(snowflakeIdGenerator.nextId());
        user.setUserName(userName);
        user.setPhone(phone);
        user.setEnabledFlag(true);
        user.setPassword(passwordEncoder.encode(password)); // 使用BCrypt加密密码
        userMapper.save(user);

        FriendRelation relation = new FriendRelation(); // 将自己添加到好友关系
        relation.setId(snowflakeIdGenerator.nextId());
        relation.setUid(user.getUid());
        relation.setFriendId(user.getUid());
        relation.setUserName(user.getUserName());
        relation.setFriendName("我");

        userMapper.saveFriendRelation(relation);
        return user;
    }
}