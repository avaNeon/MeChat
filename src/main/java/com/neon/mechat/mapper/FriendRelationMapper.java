package com.neon.mechat.mapper;

import com.neon.mechat.entity.Account;
import com.neon.mechat.entity.FriendRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRelationMapper
{
    /**
     * 插入好友关系，使用 INSERT IGNORE 保证重复添加好友时保持幂等。
     *
     * @param friendRelation 好友关系实体
     * @return 影响行数，已存在时通常为 0
     */
    int insertIgnore(FriendRelation friendRelation);

    /**
     * 按固定顺序的双方用户 ID 查询好友关系。
     *
     * @param userLowId  较小的用户 ID
     * @param userHighId 较大的用户 ID
     * @return 好友关系实体，不存在时返回 null
     */
    FriendRelation selectByUsers(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    /**
     * 查询当前用户的好友账号列表。
     *
     * @param userId 当前用户 ID
     * @return 好友账号列表
     */
    List<Account> selectFriendAccounts(@Param("userId") Long userId);
}
