package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "账号注册参数")
public class AccountRegisterDTO
{
    @Schema(description = "昵称")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;
}
