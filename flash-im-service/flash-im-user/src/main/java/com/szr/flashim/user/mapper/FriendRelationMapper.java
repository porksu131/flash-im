package com.szr.flashim.user.mapper;

import com.szr.flashim.user.pojo.FriendRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRelationMapper {
    List<FriendRelation> findFriendsByUid(@Param("uid") long uid);

    int checkFriendExists(@Param("uid") long uid, @Param("friendId") long friendId);

    int save(FriendRelation friendRelation);

    List<FriendRelation> findByUidAndFriendIds(@Param("userId") Long userId,
                                               @Param("friendIds") List<Long> friendIds);
}
