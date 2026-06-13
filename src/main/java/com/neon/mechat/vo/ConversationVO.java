package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话信息")
public class ConversationVO
{
    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "会话类型：0 单聊，1 群聊")
    private Integer type;

    @Schema(description = "群聊ID，群聊会话时有值")
    private Long groupId;

    @Schema(description = "群名称，群聊会话时有值")
    private String groupName;

    @Schema(description = "群头像，群聊会话时有值")
    private String groupAvatar;

    @Schema(description = "对方用户信息，单聊会话时有值")
    private AccountUserVO peer;

    @Schema(description = "最后一条消息")
    private MessageVO lastMessage;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数量")
    private Integer unreadCount;
}
