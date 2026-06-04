package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.AccountLoginDTO;
import com.neon.mechat.dto.AccountRegisterDTO;
import com.neon.mechat.service.AccountService;
import com.neon.mechat.vo.AccountLoginVO;
import com.neon.mechat.vo.AccountUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController
{
    private final AccountService accountService;

    /**
     * 注册账号，服务端生成用户 ID 并返回用户基础信息。
     *
     * @param accountRegisterDTO 注册参数，包含昵称、密码和头像
     * @return 注册后的用户信息
     */
    @PostMapping("/register")
    public ApiResponse<AccountUserVO> register(@Valid @RequestBody AccountRegisterDTO accountRegisterDTO)
    {
        return ApiResponse.success(accountService.register(accountRegisterDTO));
    }

    /**
     * 用户登录，校验用户 ID 和密码后签发 token，并返回 token 与用户信息。
     *
     * @param accountLoginDTO 登录参数，包含用户 ID 和密码
     * @return 登录 token 和用户信息
     */
    @PostMapping("/login")
    public ApiResponse<AccountLoginVO> login(@Valid @RequestBody AccountLoginDTO accountLoginDTO)
    {
        return ApiResponse.success(accountService.login(accountLoginDTO));
    }

    /**
     * 自动登录，校验客户端保存的 token，必要时刷新 Redis 中的 token 过期时间。
     *
     * @param token 客户端保存的登录 token
     * @return 当前 token 和用户信息
     */
    @PostMapping("/auto-login")
    public ApiResponse<AccountLoginVO> autoLogin(@RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(accountService.autoLogin(token));
    }
}
