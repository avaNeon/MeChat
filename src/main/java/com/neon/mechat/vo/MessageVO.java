package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息信息")
public class MessageVO
{
    @Schema(description = "消息ID，自增主键", example = "1")
    private Long id;

    @Schema(description = "会话ID", example = "739093770475929602")
    private Long conversationId;

    @Schema(description = "发送者用户ID", example = "739093770475929600")
    private Long senderId;

    @Schema(description = "接收者用户ID", example = "739093770475929601")
    private Long receiverId;

    @Schema(description = "客户端生成的消息ID", example = "client-msg-001")
    private String clientMessageId;

    @Schema(description = "消息类型：1 文本", example = "1")
    private Integer messageType;

    @Schema(description = "消息内容", example = "你好")
    private String content;

    @Schema(description = "发送时间", example = "2026-06-04T09:51:00")
    private LocalDateTime sendTime;
}
