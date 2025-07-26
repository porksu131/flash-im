package com.szr.flashim.user.service;

import com.szr.flashim.general.distribute.SnowflakeIdGenerator;
import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.user.mapper.FriendRelationMapper;
import com.szr.flashim.user.mapper.UserMapper;
import com.szr.flashim.user.pojo.FriendRelation;
import com.szr.flashim.user.pojo.User;
import com.szr.flashim.user.vo.FriendRelationVO;
import com.szr.flashim.user.vo.FriendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FriendRelationServiceImpl implements IFriendRelationService{
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private UserConnectionService userConnectionService;

    @Override
    public ResponseResult<List<FriendRelationVO>> getFriendsByUserId(long uid) {
        List<FriendRelationVO> friends = getFriendsWithStatus(uid);
        return ResponseResult.ok(friends);
    }

    @Transactional
    @Override
    public ResponseResult<Void> addFriend(long uid, long friendId) {
        if(friendRelationMapper.checkFriendExists(uid, friendId) > 0) {
            return ResponseResult.fail("已是好友关系");
        }

        User user = userMapper.getByUid(uid);
        User friend = userMapper.getByUid(friendId);
        if(user == null || friend == null) {
            return ResponseResult.fail("用户不存在");
        }
        // 简单实现，无需好友确认，默认双方互相添加好友，所以添加两份好友关系
        FriendRelation relation0 = buildFriendRelation(user, friend);
        friendRelationMapper.save(relation0);

        // 简单实现，无需好友确认，默认双方互相添加好友
        FriendRelation relation1 = buildFriendRelation(friend, user);
        friendRelationMapper.save(relation1);
        return ResponseResult.ok();
    }

    private FriendRelation buildFriendRelation(User user, User friend) {
        FriendRelation relation = new FriendRelation();
        relation.setId(snowflakeIdGenerator.nextId());
        relation.setUid(user.getUid());
        relation.setUserName(user.getUserName());
        relation.setFriendId(friend.getUid());
        relation.setFriendName(friend.getUserName());
        return relation;
    }

    // 获取好友列表（带在线状态）
    public List<FriendRelationVO> getFriendsWithStatus(Long userId) {
        // 1. 查询好友关系
        List<FriendRelation> relations = friendRelationMapper.findFriendsByUid(userId);

        // 2. 提取好友ID
        List<Long> friendIds = relations.stream()
                .map(FriendRelation::getFriendId)
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 批量获取好友状态
        Map<Long, FriendStatus> statusMap = userConnectionService.batchGetStatus(friendIds);

        // 4. 组合结果
        return relations.stream().map(relation -> {
            FriendRelationVO vo = new FriendRelationVO();
            vo.setId(relation.getId());
            vo.setUid(relation.getUid());
            vo.setFriendId(relation.getFriendId());
            vo.setFriendName(relation.getFriendName());

            FriendStatus status = statusMap.get(relation.getFriendId());
            if (status != null) {
                vo.setOnlineStatus(status.getOnlineStatus());
                vo.setLastActiveTime(status.getLastActiveTime());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
