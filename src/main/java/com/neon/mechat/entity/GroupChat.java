package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class GroupChat
{
    private Long id;
    private String groupName;
    private Long ownerId;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
