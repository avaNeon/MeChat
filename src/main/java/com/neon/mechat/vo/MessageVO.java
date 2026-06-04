package com.neon.mechat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO
{
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String clientMessageId;
    private Integer messageType;
    private String content;
    private LocalDateTime sendTime;
}
