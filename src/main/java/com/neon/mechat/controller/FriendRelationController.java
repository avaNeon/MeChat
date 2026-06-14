package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.AddFriendDTO;
import com.neon.mechat.service.FriendRelationService;
import com.neon.mechat.vo.AccountUserVO;
import com.neon.mechat.vo.FriendRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "好友接口", description = "好友关系管理")
public class FriendRelationController
{
    private final FriendRelationService friendRelationService;

    /**
     * 发起好友申请，支持通过用户 ID 或昵称定位对方。
     *
     * @param token        登录 token
     * @param addFriendDTO 添加好友参数
     * @return 好友申请信息
     */
    @Operation(summary = "发起好友申请",
               description = "通过好友用户ID或好友昵称发起好友申请。两者必须二选一，可附带申请留言；对方同意后才会成为好友。")
    @PostMapping
    public ApiResponse <FriendRequestVO> requestFriend(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Valid @RequestBody AddFriendDTO addFriendDTO)
    {
        return ApiResponse.success(friendRelationService.requestFriend(token, addFriendDTO));
    }

    /**
     * 查询当前用户的好友列表。
     *
     * @param token 登录 token
     * @return 好友列表
     */
    @Operation(summary = "查询好友列表", description = "返回当前用户已经建立好友关系的用户列表。")
    @GetMapping
    public ApiResponse <List <AccountUserVO>> listFriends(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(friendRelationService.listFriends(token));
    }

    /**
     * 查询当前用户收到/发送的待处理好友申请。
     *
     * @param token 登录 token
     * @return 待处理好友申请列表
     */
    @Operation(summary = "查询待处理好友申请", description = "返回当前用户收到且尚未处理的好友申请。")
    @GetMapping("/requests/pending")
    public ApiResponse <List <FriendRequestVO>> listPendingRequests(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(friendRelationService.listPendingRequests(token));
    }

    /**
     * 同意好友申请。
     *
     * @param token     登录 token
     * @param requestId 好友申请 ID
     * @return 申请发起人的基础信息
     */
    @Operation(summary = "同意好友申请", description = "只有申请接收人可以同意。接口成功后双方正式成为好友。")
    @PostMapping("/requests/{requestId}/accept")
    public ApiResponse <AccountUserVO> acceptRequest(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "好友申请ID") @PathVariable Long requestId)
    {
        return ApiResponse.success(friendRelationService.acceptRequest(token, requestId));
    }

    /**
     * 删除好友关系。
     *
     * @param token  登录 token
     * @param userId 要删除的好友用户 ID
     * @return 空响应
     */
    @Operation(summary = "删除好友", description = "删除与指定用户的好友关系，同时清理对应的单聊会话。重新添加好友后将创建新的会话。")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteFriend(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "好友用户ID") @PathVariable Long userId)
    {
        friendRelationService.deleteFriend(token, userId);
        return ApiResponse.success(null);
    }

}
