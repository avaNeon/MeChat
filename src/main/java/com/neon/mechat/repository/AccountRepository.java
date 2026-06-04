package com.neon.mechat.repository;

import com.neon.mechat.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class AccountRepository
{
    private final RedisTemplate <String, Object> redisTemplate;

    /**
     * 保存登录 token 到 Redis。
     *
     * @param token  登录 token
     * @param userId 用户 ID
     * @param ttl    token 有效期
     */
    public void saveToken(String token, Long userId, Duration ttl)
    {
        redisTemplate.opsForValue().set(RedisKeys.accountToken(token), userId, ttl);
    }

    /**
     * 根据 token 查询用户 ID。
     *
     * @param token 登录 token
     * @return 用户 ID，token 不存在或已过期时返回 null
     */
    public Long findUserIdByToken(String token)
    {
        Object value = redisTemplate.opsForValue().get(RedisKeys.accountToken(token));
        return value == null ? null : Long.valueOf(value.toString());
    }

    /**
     * 查询 token 剩余有效期。
     *
     * @param token 登录 token
     * @return token 剩余有效期；不存在或无有效期时返回 Duration.ZERO
     */
    public Duration getTokenTtl(String token)
    {
        long seconds = redisTemplate.getExpire(RedisKeys.accountToken(token));
        if (seconds < 0)
        {
            return Duration.ZERO;
        }
        return Duration.ofSeconds(seconds);
    }

    /**
     * 刷新 token 有效期，用于自动登录临近过期时延长会话。
     *
     * @param token 登录 token
     * @param ttl   新的有效期
     */
    public void refreshToken(String token, Duration ttl)
    {
        redisTemplate.expire(RedisKeys.accountToken(token), ttl);
    }

    /**
     * 删除登录 token，用于后续退出登录或强制下线。
     *
     * @param token 登录 token
     */
    public void deleteToken(String token)
    {
        redisTemplate.delete(RedisKeys.accountToken(token));
    }
}
