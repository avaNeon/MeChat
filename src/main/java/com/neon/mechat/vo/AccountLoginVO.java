package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录结果")
public class AccountLoginVO
{
    @Schema(description = "登录 token，后续请求通过请求头 token 传递", example = "2f2f6a8d0c314f9c8d5f1f25f61c37c1")
    private String token;

    @Schema(description = "用户信息")
    private AccountUserVO user;
}
