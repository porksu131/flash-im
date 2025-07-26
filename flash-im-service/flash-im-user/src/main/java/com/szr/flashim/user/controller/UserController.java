package com.szr.flashim.user.controller;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.user.pojo.User;
import com.szr.flashim.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @PostMapping("/queryAllUser")
    public ResponseResult<List<User>> queryAllUser() {
        List<User> users = userService.queryAllUsers();
        return ResponseResult.ok(users);
    }

    @PostMapping("/queryByUserName")
    public ResponseResult<List<User>> queryByUserName(@RequestBody Map<String, String> query) {
        String userName = query.get("userName");
        List<User> users = userService.queryByUserName(userName);
        return ResponseResult.ok(users);
    }

    @PostMapping("/queryByUserId")
    public ResponseResult<List<User>> queryByUserId(@RequestBody Map<String, Long> query) {
        Long id = query.get("uid");
        List<User> users = userService.queryByUserId(id);
        return ResponseResult.ok(users);
    }
}
