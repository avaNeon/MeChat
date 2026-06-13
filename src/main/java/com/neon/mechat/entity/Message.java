package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Message
{
    private Long id;
    private Long conversationId;
    private Long groupId;
    private Long senderId;
    private Long receiverId;
    private String clientMessageId;
    private Integer messageType;
    private String content;
    private String imgPath;
    private LocalDateTime sendTime;
}
