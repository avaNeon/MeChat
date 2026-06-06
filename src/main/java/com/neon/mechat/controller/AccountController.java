package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.AccountLoginDTO;
import com.neon.mechat.dto.AccountRegisterDTO;
import com.neon.mechat.service.AccountService;
import com.neon.mechat.vo.AccountLoginVO;
import com.neon.mechat.vo.AccountUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "账号接口", description = "账号注册、登录和自动登录")
public class AccountController
{
    private final AccountService accountService;

    /**
     * 注册账号，服务端生成用户 ID 并返回用户基础信息。
     *
     * @param accountRegisterDTO 注册参数，包含昵称、密码和头像
     * @return 注册后的用户信息
     */
    @Operation(summary = "注册账号", description = "注册账号时由服务端通过雪花算法生成用户ID，密码会使用 BCrypt 加密后保存。")
    @PostMapping("/register")
    public ApiResponse<AccountUserVO> register(@Valid @RequestBody AccountRegisterDTO accountRegisterDTO)
    {
        return ApiResponse.success(accountService.register(accountRegisterDTO));
    }

    /**
     * 用户登录，校验昵称和密码后签发 token，并返回 token 与用户信息。
     *
     * @param accountLoginDTO 登录参数，包含昵称和密码
     * @return 登录 token 和用户信息
     */
    @Operation(summary = "账号登录", description = "使用昵称和密码登录。登录成功后返回 token，后续 HTTP 请求通过请求头 token 传递。")
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
    @Operation(summary = "自动登录", description = "校验请求头 token 是否有效，并在 token 剩余有效期不超过配置阈值时自动续期。")
    @PostMapping("/auto-login")
    public ApiResponse<AccountLoginVO> autoLogin(@Parameter(description = "登录 token")
                                                 @RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(accountService.autoLogin(token));
    }

    /**
     * 获取当前登录用户信息。
     *
     * @param token 登录 token
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据请求头 token 返回当前登录用户基础信息。")
    @GetMapping("/me")
    public ApiResponse<AccountUserVO> me(@Parameter(description = "登录 token")
                                         @RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(accountService.getCurrentUser(token));
    }

    /**
     * 上传当前用户头像。
     *
     * @param token 登录 token
     * @param avatarFile 头像文件
     */
    @Operation(summary = "上传头像", description = "头像先保存到 tmp 目录，账号头像字段更新成功后再移动到 img 目录。")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> uploadAvatar(@Parameter(description = "登录 token")
                                          @RequestHeader(value = "token", required = false) String token,
                                          @Parameter(description = "头像文件，支持 jpg/jpeg/png/gif/webp")
                                          @RequestPart("avatarFile") MultipartFile avatarFile)
    {
        accountService.uploadAvatar(token, avatarFile);
        return ApiResponse.success(null);
    }

    /**
     * 下载头像文件。
     *
     * @param userId 用户 ID
     * @return 头像文件
     */
    @Operation(summary = "下载头像", description = "客户端只传用户ID，服务端根据账号头像字段读取 img 目录文件，tmp 目录和任意文件名都不对外暴露。")
    @GetMapping("/{userId}/avatar")
    public ResponseEntity<Resource> downloadAvatar(@Parameter(description = "用户ID")
                                                   @PathVariable Long userId)
    {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(accountService.getAvatarContentType(userId)))
                .body(accountService.downloadAvatar(userId));
    }
}
