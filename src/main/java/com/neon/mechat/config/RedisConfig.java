package com.neon.mechat.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 适用于SpringBoot3的RedisTemplate配置
 */
@Configuration
public class RedisConfig
{

    /**
     * 创建项目统一使用的 RedisTemplate，指定 key 为字符串序列化、value 为 JSON 序列化。
     *
     * @param factory Redis 连接工厂
     * @return 配置好序列化方式的 RedisTemplate
     */
    @Bean
    public RedisTemplate <String, Object> redisTemplate(RedisConnectionFactory factory)
    {

        RedisTemplate <String, Object> template = new RedisTemplate <>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 创建一个专用于 Redis 的 ObjectMapper
        ObjectMapper redisMapper = new ObjectMapper();
        redisMapper.activateDefaultTyping(redisMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);

        // 将 mapper 注入 serializer（新版构造器支持）
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisMapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}

