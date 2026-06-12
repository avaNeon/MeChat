package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "账号注册参数")
public class AccountRegisterDTO
{
    @Schema(description = "昵称", example = "Alice")
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @Schema(description = "密码", example = "12345678")
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码至少8位")
    private String password;
}
