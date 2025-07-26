package com.szr.flashim.offline.controller;

import com.szr.flashim.offline.service.OfflineMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/offline")
public class OfflineController {
    @Autowired
    private OfflineMessageService offlineMessageService;

    // 用户上线通知接口
    @PostMapping("/user-online")
    public ResponseEntity<?> handleUserOnline(@RequestParam Long userId) {
        offlineMessageService.processUserOfflineMessages(userId);
        return ResponseEntity.ok().build();
    }

    // 添加离线消息接口
    @PostMapping("/add-message")
    public ResponseEntity<?> addOfflineMessage(
            @RequestParam Long userId,
            @RequestParam Long messageId) {
        offlineMessageService.addOfflineMessage(userId, messageId);
        return ResponseEntity.ok().build();
    }
}