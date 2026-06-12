package com.neon.mechat.mapper;

import com.neon.mechat.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper
{
    /**
     * 插入消息记录，消息 ID 使用数据库自增主键生成。
     *
     * @param message 消息实体
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * 根据消息 ID 查询完整消息，主要用于插入后回查数据库生成的发送时间。
     *
     * @param id 消息 ID
     * @return 消息实体
     */
    Message selectById(@Param("id") Long id);

    /**
     * 根据发送者和客户端消息 ID 查询消息，用于客户端重试发送时保证幂等。
     *
     * @param senderId        发送者用户 ID
     * @param clientMessageId 客户端生成的消息 ID
     * @return 已存在的消息，不存在时返回 null
     */
    Message selectBySenderAndClientMessageId(@Param("senderId") Long senderId,
                                             @Param("clientMessageId") String clientMessageId);

    /**
     * 查询会话历史消息，按消息 ID 倒序返回。
     *
     * @param conversationId 会话 ID
     * @param beforeMessageId 只查询小于该 ID 的消息；为空时从最新消息开始
     * @param limit 查询数量
     * @return 消息列表
     */
    List<Message> selectConversationMessages(@Param("conversationId") Long conversationId,
                                             @Param("beforeMessageId") Long beforeMessageId,
                                             @Param("limit") Integer limit);

    /**
     * 统计当前用户在指定会话中的未读消息数量。
     *
     * @param conversationId 会话 ID
     * @param userId 当前用户 ID
     * @param lastSyncMessageId 当前用户已同步到的消息 ID
     * @return 未读消息数量
     */
    int countUnreadMessages(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId,
                            @Param("lastSyncMessageId") Long lastSyncMessageId);
}
