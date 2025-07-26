package com.szr.flashim.core.netty.handler;

import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.util.ChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImMessageEncoder extends MessageToByteEncoder<ImMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImMessageEncoder.class);

    @Override
    public void encode(ChannelHandlerContext ctx, ImMessage imMessage, ByteBuf outByteBuf) throws Exception {
        try {
            ImCustomerSerialize.encode(imMessage, outByteBuf);
        } catch (Exception e) {
            LOGGER.error("encode exception, {}", ChannelUtil.parseChannelRemoteAddr(ctx.channel()), e);
            if (imMessage != null) {
                LOGGER.error(imMessage.toString());
            }
            ChannelUtil.closeChannel(ctx.channel());
        }
    }
}
