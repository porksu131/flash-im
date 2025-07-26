package com.szr.flashim.user.mapper;

import com.szr.flashim.user.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    User getByUserName(@Param("userName") String userName);
    User getByUid(@Param("uid") long uid);
    int save(User user);
    List<User> queryAllUsers();

    List<User> queryByUserName(@Param("userName") String userName);
}
