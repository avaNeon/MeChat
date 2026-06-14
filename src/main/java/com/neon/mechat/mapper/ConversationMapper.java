package com.neon.mechat.mapper;

import com.neon.mechat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper
{
    int insertIgnore(Conversation conversation);

    int insertGroup(Conversation conversation);

    Conversation selectByUsers(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    Conversation selectById(@Param("id") Long id);

    List<Conversation> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    Conversation selectByGroupId(@Param("groupId") Long groupId);

    int updateLastMessage(@Param("conversationId") Long conversationId, @Param("messageId") Long messageId);

    int updateUserLastSyncMessage(@Param("conversationId") Long conversationId,
                                  @Param("userId") Long userId,
                                  @Param("messageId") Long messageId);

    int deleteByGroupId(@Param("groupId") Long groupId);

    int deleteByUsers(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);
}
