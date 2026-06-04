package com.neon.mechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageDTO
{
    @NotNull(message = "接收者用户ID不能为空")
    private Long receiverId;

    @NotBlank(message = "客户端消息ID不能为空")
    private String clientMessageId;

    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
