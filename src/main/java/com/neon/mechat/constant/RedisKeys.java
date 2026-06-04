package com.neon.mechat.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisKeys
{
    private static final String ACCOUNT_TOKEN_PREFIX = "account:token:";

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
}
