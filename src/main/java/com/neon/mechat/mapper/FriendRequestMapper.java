package com.neon.mechat.mapper;

import com.neon.mechat.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRequestMapper
{
    /**
     * 插入或重置一对用户之间的好友申请。
     *
     * @param friendRequest 好友申请
     * @return 影响行数
     */
    int insertOrResetPending(FriendRequest friendRequest);

    /**
     * 按双方固定顺序查询好友申请。
     *
     * @param userLowId  较小的用户 ID
     * @param userHighId 较大的用户 ID
     * @return 好友申请，不存在时返回 null
     */
    FriendRequest selectByUsers(@Param("userLowId") Long userLowId, @Param("userHighId") Long userHighId);

    /**
     * 按申请 ID 查询并加行锁，用于处理申请。
     *
     * @param id 申请 ID
     * @return 好友申请，不存在时返回 null
     */
    FriendRequest selectByIdForUpdate(@Param("id") Long id);

    /**
     * 查询与当前用户相关的待处理好友申请（发出的 + 收到的）。
     *
     * @param userId 当前用户 ID
     * @param status 申请状态
     * @return 好友申请列表
     */
    List<FriendRequest> selectPendingByUser(@Param("userId") Long userId,
                                            @Param("status") Integer status);

    /**
     * 更新好友申请状态。
     *
     * @param id             申请 ID
     * @param status         目标状态
     * @param expectedStatus 当前期望状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("expectedStatus") Integer expectedStatus);
}
