package com.szr.flashim.offline.mapper;

import com.szr.flashim.offline.pojo.ChatMessagePojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessagePojo message);
    void updateStatus(@Param("messageId") long messageId, @Param("status") int status);
    ChatMessagePojo selectById(long messageId);
    List<ChatMessagePojo> selectUnreadMessages(@Param("messageTo") long messageTo,
                                               @Param("messageFrom") long messageFrom,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);
    int countUnreadMessages(@Param("messageTo") long messageTo,
                            @Param("messageFrom") long messageFrom);

    List<ChatMessagePojo> batchSelectMessages(@Param("ids") List<String> ids);
}
