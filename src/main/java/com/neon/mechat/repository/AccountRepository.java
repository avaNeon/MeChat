package com.neon.mechat.repository;

import com.neon.mechat.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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

    /**
     * 查询用户当日已上传字节数。
     *
     * @param userId 用户 ID
     * @return 已上传字节数
     */
    public long getDailyUploadBytes(Long userId)
    {
        Object value = redisTemplate.opsForValue().get(RedisKeys.uploadQuota(userId, LocalDate.now()));
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    /**
     * 累加用户当日上传字节数，并设置 key 在当天结束时自动过期。
     *
     * @param userId 用户 ID
     * @param bytes  本次上传字节数
     */
    public void addDailyUploadBytes(Long userId, long bytes)
    {
        String key = RedisKeys.uploadQuota(userId, LocalDate.now());
        redisTemplate.opsForValue().increment(key, bytes);
        long secondsUntilMidnight = LocalDateTime.now().until(
                LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
        redisTemplate.expire(key, Duration.ofSeconds(secondsUntilMidnight));
    }
}
