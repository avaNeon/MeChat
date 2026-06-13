package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.CreateGroupDTO;
import com.neon.mechat.dto.SendGroupMessageDTO;
import com.neon.mechat.service.ConversationService;
import com.neon.mechat.service.GroupChatService;
import com.neon.mechat.service.MessageService;
import com.neon.mechat.vo.ConversationMessagePageVO;
import com.neon.mechat.vo.GroupChatVO;
import com.neon.mechat.vo.GroupMemberVO;
import com.neon.mechat.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "群聊接口", description = "群聊管理")
public class GroupChatController
{
    private final GroupChatService groupChatService;
    private final MessageService messageService;
    private final ConversationService conversationService;

    @Operation(summary = "创建群聊",
               description = "创建一个新群聊，创建者自动成为群主，同时可指定初始成员。所有初始成员必须为创建者的好友。")
    @PostMapping
    public ApiResponse<GroupChatVO> createGroup(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Valid @RequestBody CreateGroupDTO createGroupDTO)
    {
        return ApiResponse.success(groupChatService.createGroup(token, createGroupDTO));
    }

    @Operation(summary = "拉人进群", description = "将好友拉入群聊，任何群成员都可以拉人。被拉用户必须为操作者的好友。")
    @PostMapping("/{groupId}/members")
    public ApiResponse<Void> addMembers(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId,
            @Parameter(description = "要拉入群的用户ID列表") @RequestBody List<Long> userIds)
    {
        groupChatService.addMembers(token, groupId, userIds);
        return ApiResponse.success(null);
    }

    @Operation(summary = "踢人出群", description = "群主踢出指定群成员，仅支持单个用户ID。群主不能踢自己。")
    @DeleteMapping("/{groupId}/members/{userId}")
    public ApiResponse<Void> kickMember(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId,
            @Parameter(description = "要踢出的用户ID") @PathVariable Long userId)
    {
        groupChatService.kickMember(token, groupId, userId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "查询群成员列表", description = "查询指定群聊中所有成员的用户信息，只有群成员可以查看。")
    @GetMapping("/{groupId}/members")
    public ApiResponse<List<GroupMemberVO>> listMembers(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId)
    {
        return ApiResponse.success(groupChatService.listMembers(token, groupId));
    }

    @Operation(summary = "查询我的群聊列表", description = "查询当前用户所属的所有群聊。")
    @GetMapping
    public ApiResponse<List<GroupChatVO>> listMyGroups(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token)
    {
        return ApiResponse.success(groupChatService.listMyGroups(token));
    }

    @Operation(summary = "发送群聊消息", description = "向群聊发送消息，所有群成员均可发送和接收。")
    @PostMapping("/{groupId}/messages")
    public ApiResponse<MessageVO> sendGroupMessage(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId,
            @Valid @RequestBody SendGroupMessageDTO sendGroupMessageDTO)
    {
        return ApiResponse.success(messageService.sendGroupMessage(token, groupId, sendGroupMessageDTO));
    }

    @Operation(summary = "查询群聊消息历史", description = "分页查询群聊历史消息，按发送时间倒序。")
    @GetMapping("/{groupId}/messages")
    public ApiResponse<ConversationMessagePageVO> listGroupMessages(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId,
            @Parameter(description = "只查询该消息 ID 之前的消息") @RequestParam(required = false) Long beforeMessageId,
            @Parameter(description = "每页数量，默认 30") @RequestParam(required = false) Integer limit)
    {
        return ApiResponse.success(conversationService.listGroupMessages(token, groupId, beforeMessageId, limit));
    }

    @Operation(summary = "标记群聊已读", description = "将群聊消息标记为已读。")
    @PostMapping("/{groupId}/read")
    public ApiResponse<Void> markGroupRead(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "群聊ID") @PathVariable Long groupId)
    {
        conversationService.markGroupRead(token, groupId);
        return ApiResponse.success(null);
    }
}
