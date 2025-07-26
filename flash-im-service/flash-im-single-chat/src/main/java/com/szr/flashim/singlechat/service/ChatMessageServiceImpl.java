package com.szr.flashim.singlechat.service;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.singlechat.contants.MessageStatus;
import com.szr.flashim.singlechat.mapper.ChatMessageMapper;
import com.szr.flashim.singlechat.pojo.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageServiceImpl implements IChatMessageService {
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ResponseResult<Void> saveMessage(ChatMessage message) {
        message.setCreateTime(System.currentTimeMillis());
        message.setStatus(MessageStatus.UN_SEND); // 默认未发送
        chatMessageMapper.insert(message);
        return ResponseResult.ok();
    }

    @Override
    public ResponseResult<Void> updateMessageStatus(long messageId, int status) {
        chatMessageMapper.updateStatus(messageId, status);
        return ResponseResult.ok();
    }

    @Override
    public ResponseResult<ChatMessage> getMessageById(long messageId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        return ResponseResult.ok(message);
    }

    @Override
    public ResponseResult<Map<String, Object>> getUnreadMessages(long messageTo, long messageFrom, int page, int size) {
        int offset = (page - 1) * size;
        List<ChatMessage> messages = chatMessageMapper.selectUnreadMessages(messageTo, messageFrom, offset, size);
        int total = chatMessageMapper.countUnreadMessages(messageTo, messageFrom);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pageIndex", page);
        result.put("pageSize", size);
        result.put("pageList", messages);

        return ResponseResult.ok(result);
    }
}
