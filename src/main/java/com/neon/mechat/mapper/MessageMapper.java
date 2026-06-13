package com.neon.mechat.mapper;

import com.neon.mechat.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper
{
    int insert(Message message);

    Message selectById(@Param("id") Long id);

    Message selectBySenderAndClientMessageId(@Param("senderId") Long senderId,
                                             @Param("clientMessageId") String clientMessageId);

    List<Message> selectConversationMessages(@Param("conversationId") Long conversationId,
                                             @Param("beforeMessageId") Long beforeMessageId,
                                             @Param("limit") Integer limit);

    List<Message> selectGroupMessages(@Param("groupId") Long groupId,
                                      @Param("beforeMessageId") Long beforeMessageId,
                                      @Param("limit") Integer limit);

    int countUnreadMessages(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId,
                            @Param("lastSyncMessageId") Long lastSyncMessageId);

    int countGroupUnreadMessages(@Param("groupId") Long groupId,
                                 @Param("userId") Long userId,
                                 @Param("lastReadMessageId") Long lastReadMessageId);
}
