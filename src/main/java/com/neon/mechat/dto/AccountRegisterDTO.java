package com.neon.mechat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountRegisterDTO
{
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String avatar;
}
