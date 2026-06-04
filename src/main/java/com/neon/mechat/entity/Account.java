package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Account
{
    private Long userId;
    private String nickname;
    private String password;
    private String avatar;
    private LocalDateTime createdAt;
}
