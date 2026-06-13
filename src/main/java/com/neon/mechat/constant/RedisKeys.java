package com.neon.mechat.constant;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class RedisKeys
{
    private static final String ACCOUNT_TOKEN_PREFIX = "account:token:";
    private static final String UPLOAD_QUOTA_PREFIX = "account:upload:quota:";

    /**
     * 生成账号登录 token 在 Redis 中的完整 key，集中管理 key 格式避免散落硬编码。
     *
     * @param token 登录 token
     * @return Redis key
     */
    public String accountToken(String token)
    {
        return ACCOUNT_TOKEN_PREFIX + token;
    }

    /**
     * 生成用户上传每日配额 key。
     *
     * @param userId 用户 ID
     * @param date   配额日期
     * @return Redis key
     */
    public String uploadQuota(Long userId, LocalDate date)
    {
        return UPLOAD_QUOTA_PREFIX + userId + ":" + date;
    }
}
