package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "群聊信息")
public class GroupChatVO
{
    @Schema(description = "群聊ID")
    private Long id;

    @Schema(description = "群名称")
    private String groupName;

    @Schema(description = "群主")
    private AccountUserVO owner;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "成员数量")
    private int memberCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
