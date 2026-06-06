package com.neon.mechat.mapper;

import com.neon.mechat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper
{
    /**
     * 插入单聊会话，使用 INSERT IGNORE 避免并发创建同一对用户会话时报唯一键冲突。
     *
     * @param conversation 会话实体
     * @return 影响行数，已存在时通常为 0
     */
    int insertIgnore(Conversation conversation);

    /**
     * 按固定顺序的双方用户 ID 查询单聊会话，避免 A-B 和 B-A 产生两条会话。
     *
     * @param userLowId  较小的用户 ID
     * @param userHighId 较大的用户 ID
     * @return 会话实体，不存在时返回 null
     */
    Conversation selectByUsers(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    /**
     * 根据会话 ID 查询会话。
     *
     * @param id 会话 ID
     * @return 会话实体，不存在时返回 null
     */
    Conversation selectById(@Param("id") Long id);

    /**
     * 查询当前用户参与的会话列表。
     *
     * @param userId 当前用户 ID
     * @param limit  查询数量
     * @return 会话列表
     */
    List<Conversation> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 更新会话的最后一条消息，供会话列表排序和后续同步入口使用。
     *
     * @param conversationId 会话 ID
     * @param messageId      最后一条消息 ID
     * @return 影响行数
     */
    int updateLastMessage(@Param("conversationId") Long conversationId, @Param("messageId") Long messageId);

    /**
     * 更新指定用户在会话中的最后同步消息 ID。
     *
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param messageId      已同步到的消息 ID
     * @return 影响行数
     */
    int updateUserLastSyncMessage(@Param("conversationId") Long conversationId,
                                  @Param("userId") Long userId,
                                  @Param("messageId") Long messageId);
}
