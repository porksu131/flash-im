package com.szr.flashim.core.netty.handler;

import com.szr.flashim.general.model.ImMessage;
import com.szr.flashim.core.netty.exception.MessageConvertException;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ImCustomerSerialize {
    public static ImMessage decode(ByteBuf inByteBuf) throws MessageConvertException {
        ImMessage imMessage = new ImMessage();
        readHeader(imMessage, inByteBuf);
        if (inByteBuf.readableBytes() > 0) {
            imMessage.setBody(new byte[inByteBuf.readableBytes()]);
            inByteBuf.readBytes(imMessage.getBody());
        }
        return imMessage;
    }

    public static void encode(ImMessage imMessage, ByteBuf outByteBuf) {
        int beginIndex = outByteBuf.writerIndex();
        // 占位，首4个字节存储整个消息的长度
        outByteBuf.writeInt(0);
        // 写入headerLength + headerContent
        int headerLength = writeHeader(imMessage, outByteBuf);
        // 写入body
        int bodyLength =  0;
        if (imMessage.getBody() != null) {
            bodyLength = imMessage.getBody().length;
            outByteBuf.writeBytes(imMessage.getBody());
        }
        // 修改整个消息的长度
        outByteBuf.setInt(beginIndex,  headerLength + bodyLength);
    }

    public static void readHeader(ImMessage imMessage, ByteBuf outByteBuf) throws MessageConvertException{
        int headerLength = outByteBuf.readInt();
        if (headerLength > outByteBuf.readableBytes()) {
            throw new MessageConvertException("ImMessage protocol decoding failed, header length: " + headerLength + ", but message length: " + outByteBuf.readableBytes());
        }
        // int version
        imMessage.setVersion(outByteBuf.readInt());
        // int serialType
        imMessage.setSerialType(outByteBuf.readInt());
        // int msgId
        imMessage.setMsgId(outByteBuf.readLong());
        // int bizType
        imMessage.setBizType(outByteBuf.readInt());
        // int subBizType
        imMessage.setSubBizType(outByteBuf.readInt());
        // int msgType
        imMessage.setMsgType(outByteBuf.readInt());
        // HashMap<String, String> extraFields
        int extraFieldsLength = outByteBuf.readInt();
        if (extraFieldsLength > 0) {
            if (extraFieldsLength > headerLength) {
                throw new MessageConvertException("ImMessage protocol decoding failed, extraFields length: " + extraFieldsLength + ", but header length: " + headerLength);
            }
            imMessage.setExtraFields(mapDeserialize(outByteBuf, extraFieldsLength));
        }
    }


    public static int writeHeader(ImMessage imMessage, ByteBuf outByteBuf) {
        int beginIndex = outByteBuf.writerIndex();
        // 占位，用于记录整个header的长度
        outByteBuf.writeInt(0);

        // int version
        outByteBuf.writeInt(imMessage.getVersion());
        // int serialType
        outByteBuf.writeInt(imMessage.getSerialType());
        // int msgId
        outByteBuf.writeLong(imMessage.getMsgId());
        // int bizType
        outByteBuf.writeInt(imMessage.getBizType());
        // int subBizType
        outByteBuf.writeInt(imMessage.getSubBizType());
        // int msgType
        outByteBuf.writeInt(imMessage.getMsgType());
        // HashMap<String, String> extraFields

        int mapIndex = outByteBuf.writerIndex();
        // 占位，用于记录extraFieldsMap的长度
        outByteBuf.writeInt(0);

        HashMap<String, String> extraFieldsMap = imMessage.getExtraFields();
        if (extraFieldsMap != null && !extraFieldsMap.isEmpty()) {
            extraFieldsMap.forEach((k, v) -> {
                if (k != null && v != null) {
                    writeStr(outByteBuf, true, k); // 对于key，先用short记录长度后，再记录key值
                    writeStr(outByteBuf, false, v); // 对于value，先用int记录长度后，再记录value值
                }
            });
        }

        int endIndex = outByteBuf.writerIndex();
        // 修改extraFieldsMap的长度
        outByteBuf.setInt(mapIndex, endIndex - mapIndex - 4);
        // 修改header的长度
        outByteBuf.setInt(beginIndex, endIndex - beginIndex);

        return endIndex - beginIndex;
    }

    public static HashMap<String, String> mapDeserialize(ByteBuf byteBuffer, int len) throws MessageConvertException {
        HashMap<String, String> map = new HashMap<>(128);
        int endIndex = byteBuffer.readerIndex() + len;

        while (byteBuffer.readerIndex() < endIndex) {
            String k = readStr(byteBuffer, true, len);
            String v = readStr(byteBuffer, false, len);
            map.put(k, v);
        }
        return map;
    }


    private static String readStr(ByteBuf buf, boolean useShortLength, int limit) throws MessageConvertException {
        int len = useShortLength ? buf.readShort() : buf.readInt();
        if (len == 0) {
            return null;
        }
        if (len > limit) {
            throw new MessageConvertException("string length exceed limit:" + limit);
        }
        CharSequence cs = buf.readCharSequence(len, StandardCharsets.UTF_8);
        return cs == null ? null : cs.toString();
    }

    public static void writeStr(ByteBuf buf, boolean useShortLength, String str) {
        int lenIndex = buf.writerIndex();
        if (useShortLength) {
            buf.writeShort(0);
        } else {
            buf.writeInt(0);
        }
        int len = buf.writeCharSequence(str, StandardCharsets.UTF_8);
        if (useShortLength) {
            buf.setShort(lenIndex, len);
        } else {
            buf.setInt(lenIndex, len);
        }
    }
}
