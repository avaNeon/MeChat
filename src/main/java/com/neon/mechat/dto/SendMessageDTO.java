package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发送单聊消息参数")
public class SendMessageDTO
{
    @Schema(description = "接收者用户ID")
    @NotNull(message = "接收者用户ID不能为空")
    private Long receiverId;

    @Schema(description = "客户端生成的消息ID，用于发送幂等", example = "client-msg-001")
    @NotBlank(message = "客户端消息ID不能为空")
    private String clientMessageId;

    @Schema(description = "消息类型：1 文本", example = "1")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @Schema(description = "消息内容", example = "你好")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容不能超过5000个字符")
    private String content;
}
