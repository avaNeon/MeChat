package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Conversation
{
    private Long id;
    private Long userLowId;
    private Long userHighId;
    private Long userLowLastSyncMessageId;
    private Long userHighLastSyncMessageId;
    private Long lastMessageId;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
