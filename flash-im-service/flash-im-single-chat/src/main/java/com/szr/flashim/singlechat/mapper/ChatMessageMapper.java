package com.szr.flashim.singlechat.mapper;

import com.szr.flashim.singlechat.pojo.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage message);

    void updateStatus(@Param("messageId") Long messageId, @Param("status") int status);

    ChatMessage selectById(Long messageId);

    List<ChatMessage> selectUnreadMessages(@Param("messageTo") Long messageTo,
                                           @Param("messageFrom") Long messageFrom,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    int countUnreadMessages(@Param("messageTo") Long messageTo,
                            @Param("messageFrom") Long messageFrom);

    int batchUpdateStatusWithTempTable(@Param("ids") List<Long> ids,
                                       @Param("messageTo") long messageTo,
                                       @Param("status") int status,
                                       @Param("readTime") long readTime);

    int batchUpdateStatus(@Param("ids") List<Long> ids,
                                       @Param("messageTo") long messageTo,
                                       @Param("status") int status,
                                       @Param("readTime") long readTime);
}
