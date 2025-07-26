package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public interface NettyReceiveChatMessageInvoke {
    void onReceiveMessage(List<ChatMessage> chatMessages);
}
