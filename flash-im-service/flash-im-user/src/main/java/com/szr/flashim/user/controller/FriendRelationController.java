package com.szr.flashim.user.controller;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.user.pojo.FriendRelation;
import com.szr.flashim.user.service.IFriendRelationService;
import com.szr.flashim.user.vo.FriendRelationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friend")
public class FriendRelationController {
    @Autowired
    private IFriendRelationService friendRelationService;

    @PostMapping("/list")
    public ResponseResult<List<FriendRelationVO>> list(@RequestBody Map<String, Long> reqMap) {
        Long uid = reqMap.get("uid");
        return friendRelationService.getFriendsByUserId(uid);
    }

    @PostMapping("/add")
    public ResponseResult<Void> add(@RequestBody Map<String, Long> reqMap) {
        Long uid = reqMap.get("uid");
        Long friendId = reqMap.get("friendId");
        return friendRelationService.addFriend(uid, friendId);
    }
}
