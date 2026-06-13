package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class GroupChatMember
{
    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role;
    private Long lastReadMessageId;
    private LocalDateTime joinTime;
}
