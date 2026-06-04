package com.neon.mechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig
{

    /**
     * 提供 BCrypt 密码加密器，用于注册时加密密码和登录时校验密码。
     *
     * @return BCrypt 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
