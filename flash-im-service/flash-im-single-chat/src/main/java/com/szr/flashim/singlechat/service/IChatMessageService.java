package com.szr.flashim.singlechat.service;

import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.singlechat.pojo.ChatMessage;

import java.util.Map;

public interface IChatMessageService {
    ResponseResult<Void> saveMessage(ChatMessage message);

    ResponseResult<Void>  updateMessageStatus(long messageId, int status);

    ResponseResult<ChatMessage> getMessageById(long messageId);

    ResponseResult<Map<String, Object>> getUnreadMessages(long messageTo, long messageFrom, int page, int size);
}