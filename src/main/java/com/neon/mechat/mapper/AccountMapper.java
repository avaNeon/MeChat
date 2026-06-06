package com.neon.mechat.mapper;

import com.neon.mechat.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper
{
    /**
     * 插入账号数据，userId 由业务层雪花算法生成。
     *
     * @param account 账号实体
     * @return 影响行数
     */
    int insert(Account account);

    /**
     * 根据用户 ID 查询账号，用于登录校验、token 自动登录和接收者存在性检查。
     *
     * @param userId 用户 ID
     * @return 账号实体，不存在时返回 null
     */
    Account selectByUserId(@Param("userId") Long userId);

    /**
     * 根据昵称查询账号，用于登录校验。
     *
     * @param nickname 昵称
     * @return 账号实体，不存在时返回 null
     */
    Account selectByNickname(@Param("nickname") String nickname);

    /**
     * 更新用户头像文件名。
     *
     * @param userId 用户 ID
     * @param avatar 头像文件名
     * @return 影响行数
     */
    int updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    /**
     * 判断用户 ID 是否存在，保留给需要主动校验账号唯一性的业务使用。
     *
     * @param userId 用户 ID
     * @return true 表示已存在
     */
    boolean existsByUserId(@Param("userId") Long userId);

    /**
     * 判断昵称是否存在，用于注册前校验昵称唯一性。
     *
     * @param nickname 昵称
     * @return true 表示已存在
     */
    boolean existsByNickname(@Param("nickname") String nickname);
}
