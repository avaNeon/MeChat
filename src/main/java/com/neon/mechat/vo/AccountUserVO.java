package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号用户信息")
public class AccountUserVO
{
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像文件名，仅用于展示是否已设置头像；下载头像请请求 /accounts/{userId}/avatar")
    private String avatar;
}
