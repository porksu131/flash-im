package com.szr.flashim.dispatch.processor;

import com.szr.flashim.core.netty.processor.NettyNotifyProcessor;
import com.szr.flashim.general.enumeration.BizType;
import com.szr.flashim.general.enumeration.NotifyType;
import com.szr.flashim.general.enumeration.SubBizType;
import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.general.model.protoc.AuthNotify;
import com.szr.flashim.mq.starter.MqClientManager;
import com.szr.flashim.mq.starter.MqEventType;
import com.szr.flashim.mq.starter.MqTopicConstant;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthNotifyProcessor implements NettyNotifyProcessor {
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthNotifyProcessor.class);

    @Autowired
    private MqClientManager mqClientManager;

    @Override
    public int bizType() {
        return BizType.AUTH.getCode();
    }

    @Override
    public boolean rejectProcess(ImMessage request) {
        return !(SubBizType.LOGIN.equals(request.getSubBizType())
                || SubBizType.LOGOUT.equals(request.getSubBizType()));
    }

    @Override
    public void processNotify(ChannelHandlerContext ctx, ImMessage message) throws Exception {
        AuthNotify authNotify = AuthNotify.parseFrom(message.getBody());
        pushOnlineNotifyMessageToMq(authNotify);
    }

    public void pushOnlineNotifyMessageToMq(AuthNotify authNotify) {
        String topic = MqTopicConstant.USR_AUTH_CHANGE_TOPIC;
        MqEventType mqEventType = MqEventType.USER_ONLINE_CHANGE_NOTIFY;
        String onlineStatusText = NotifyType.ON_LINE.getCode() == authNotify.getOperationType() ? "上线" : "离线";
        mqClientManager.sendAsyncMessage(topic, mqEventType, authNotify.toByteArray(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                LOGGER.info("用户[{}]{}通知推送成功:Topic:[{}]，msgId:[{}]", authNotify.getUid(), onlineStatusText, topic, sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                LOGGER.error("用户[{}]{}通知推送失败:Topic:[{}], 原因：[{}]", authNotify.getUid(), onlineStatusText, topic, throwable.getMessage());
            }
        });
    }
}
