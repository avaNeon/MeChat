package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "添加好友参数")
public class AddFriendDTO
{
    @Schema(description = "好友用户ID，与好友昵称二选一")
    private Long friendUserId;

    @Schema(description = "好友昵称，与好友用户ID二选一")
    private String friendNickname;

    @Schema(description = "好友申请留言")
    @Size(max = 255, message = "好友申请留言不能超过255个字符")
    private String message;
}
