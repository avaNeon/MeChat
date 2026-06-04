package com.neon.mechat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "mechat.account")
public class AccountProperties
{
    private Duration loginTokenTtl = Duration.ofDays(7);
    private Duration loginTokenRenewThreshold = Duration.ofDays(1);
}
