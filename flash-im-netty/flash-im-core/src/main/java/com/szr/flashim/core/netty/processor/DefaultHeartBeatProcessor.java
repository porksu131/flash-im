package com.szr.flashim.core.netty.processor;

import com.szr.flashim.general.constant.ResponseCode;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.model.ImMessage;
import io.netty.channel.ChannelHandlerContext;

public class DefaultHeartBeatProcessor implements NettyClientRequestProcessor {

    @Override
    public int bizType() {
        return BizType.HEART_BEAT.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return request.getBizType() != BizType.HEART_BEAT.getCode();
    }

    @Override
    public ImMessage processRequest(ChannelHandlerContext ctx, ImMessage request) throws Exception {
        return ImMessage.createMessageResponse(request, ResponseCode.SUCCESS, "心跳接收成功");
    }
}
