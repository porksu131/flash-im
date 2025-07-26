package com.szr.flashim.singlechat.controller;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.singlechat.pojo.ChatMessage;
import com.szr.flashim.singlechat.service.IChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {
    @Autowired
    private IChatMessageService chatMessageService;

    @PostMapping("/save")
    public ResponseResult<Void> saveMessage(@RequestBody ChatMessage message) {
        return chatMessageService.saveMessage(message);
    }

    @PostMapping("/updateStatus")
    public ResponseResult<Void> updateMessageStatus(@RequestParam long messageId,
                                          @RequestParam int status) {
        return chatMessageService.updateMessageStatus(messageId, status);
    }

    @PostMapping("/getById")
    public ResponseResult<ChatMessage> getMessageById(@RequestParam long messageId) {
        return chatMessageService.getMessageById(messageId);
    }

    @PostMapping("/unreadMessages")
    public ResponseResult<Map<String, Object>> getUnreadMessages(@RequestParam long messageTo,
                                                                 @RequestParam long messageFrom,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return chatMessageService.getUnreadMessages(messageTo, messageFrom, page, size);
    }
}