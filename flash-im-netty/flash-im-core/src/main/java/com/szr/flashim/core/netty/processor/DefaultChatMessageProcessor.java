package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.BatchMessage;
import com.szr.flashim.general.model.protoc.ChatMessage;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultChatMessageProcessor implements NettyClientRequestProcessor {

    private final NettyReceiveChatMessageInvoke receiveChatMessageInvoke;

    public DefaultChatMessageProcessor(NettyReceiveChatMessageInvoke receiveMessageInvoke) {
        this.receiveChatMessageInvoke = receiveMessageInvoke;
    }

    @Override
    public int bizType() {
        return BizType.SINGLE_CHAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(SubBizType.BATCH_MSG.equals(request.getSubBizType())
                || SubBizType.SINGLE_MSG.equals(request.getSubBizType()));
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) throws Exception {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        if (SubBizType.BATCH_MSG.equals(request.getSubBizType())) {
            BatchMessage batchMessage = BatchMessage.parseFrom(request.getBody());
            chatMessageList = batchMessage.getChatMessagesList();
        } else if (SubBizType.SINGLE_MSG.equals(request.getSubBizType())) {
            ChatMessage chatMessage = ChatMessage.parseFrom(request.getBody());
            chatMessageList = Collections.singletonList(chatMessage);
        }

        if (receiveChatMessageInvoke != null) {
            receiveChatMessageInvoke.onReceiveMessage(chatMessageList); // 如果比较耗时 建议异步处理
        }
        return ImMessage.createMessageResponse(request, ResponseCode.SUCCESS, "已成功接收消息");
    }
}
