package com.neon.mechat.config;

import com.neon.mechat.config.properties.SnowflakeProperties;
import com.neon.mechat.support.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig
{
    /**
     * 创建雪花 ID 生成器，供账号 ID 和会话 ID 这类业务主键使用。
     *
     * @param snowflakeProperties 雪花算法机器号和数据中心配置
     * @return 雪花 ID 生成器
     */
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeProperties snowflakeProperties)
    {
        return new SnowflakeIdGenerator(
                snowflakeProperties.getWorkerId(),
                snowflakeProperties.getDatacenterId()
        );
    }
}
