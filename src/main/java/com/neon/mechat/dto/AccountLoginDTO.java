package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "账号登录参数")
public class AccountLoginDTO
{
    @Schema(description = "用户ID", example = "739093770475929600")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
