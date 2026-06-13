package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建群聊参数")
public class CreateGroupDTO
{
    @Schema(description = "群名称")
    @NotBlank(message = "群名称不能为空")
    @Size(max = 255, message = "群名称不能超过255个字符")
    private String groupName;

    @Schema(description = "初始成员用户ID列表，不能为空")
    @NotEmpty(message = "初始成员不能为空")
    private List<Long> memberUserIds;
}
