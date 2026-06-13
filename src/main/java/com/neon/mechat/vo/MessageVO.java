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
    @Schema(description = "消息ID，自增主键")
    private Long id;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "群聊ID，群聊消息时有值")
    private Long groupId;

    @Schema(description = "发送者用户ID")
    private Long senderId;

    @Schema(description = "接收者用户ID")
    private Long receiverId;

    @Schema(description = "客户端生成的消息ID", example = "client-msg-001")
    private String clientMessageId;

    @Schema(description = "消息类型：1 文本", example = "1")
    private Integer messageType;

    @Schema(description = "消息内容", example = "你好")
    private String content;

    @Schema(description = "聊天图片路径，相对于 storage 目录", example = "file/msg/123_abc.png")
    private String imgPath;

    @Schema(description = "发送时间", example = "2026-06-04T09:51:00")
    private LocalDateTime sendTime;
}
