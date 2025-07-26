package com.szr.flashim.auth.mapper;

import com.szr.flashim.auth.model.pojo.FriendRelation;
import com.szr.flashim.auth.model.pojo.UserInfo;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    UserInfo getByUserName(@Param("userName") String userName);

    UserInfo getByUid(@Param("uid") long uid);

    int save(UserInfo user);

    int saveFriendRelation(FriendRelation friendRelation);
}
