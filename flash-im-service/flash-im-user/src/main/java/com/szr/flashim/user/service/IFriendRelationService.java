package com.szr.flashim.user.service;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.user.vo.FriendRelationVO;

import java.util.List;

public interface IFriendRelationService {
    ResponseResult<List<FriendRelationVO>> getFriendsByUserId(long uid);

    ResponseResult<Void> addFriend(long uid, long friendId);
}