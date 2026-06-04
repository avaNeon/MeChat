package com.neon.mechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mechat.snowflake")
public class SnowflakeProperties
{
    private long workerId = 1;
    private long datacenterId = 1;
}
