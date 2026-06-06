package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.service.ConversationService;
import com.neon.mechat.vo.ConversationMessagePageVO;
import com.neon.mechat.vo.ConversationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "会话接口", description = "首页会话列表、会话历史消息和已读状态")
public class ConversationController
{
    private final ConversationService conversationService;

    /**
     * 查询首页会话列表。
     *
     * @param token 登录 token
     * @param limit 查询数量
     * @return 会话列表
     */
    @Operation(summary = "查询会话列表", description = "返回当前用户参与的单聊会话，按最后消息时间倒序排列。")
    @GetMapping
    public ApiResponse<List<ConversationVO>> listConversations(@Parameter(description = "登录 token")
                                                               @RequestHeader(value = "token", required = false) String token,
                                                               @Parameter(description = "本次最多返回会话数量，默认 50，最大 100")
                                                               @RequestParam(value = "limit", required = false) Integer limit)
    {
        return ApiResponse.success(conversationService.listConversations(token, limit));
    }

    /**
     * 查询会话历史消息。
     *
     * @param token           登录 token
     * @param conversationId  会话 ID
     * @param beforeMessageId 只查询该消息 ID 之前的消息
     * @param limit           查询数量
     * @return 历史消息分页结果
     */
    @Operation(summary = "查询会话历史消息", description = "用于进入聊天页和向上翻页。返回消息按发送时间正序排列。")
    @GetMapping("/{conversationId}/messages")
    public ApiResponse<ConversationMessagePageVO> listMessages(@Parameter(description = "登录 token")
                                                               @RequestHeader(value = "token", required = false) String token,
                                                               @Parameter(description = "会话ID")
                                                               @PathVariable Long conversationId,
                                                               @Parameter(description = "只查询该消息ID之前的消息，不传则从最新消息开始")
                                                               @RequestParam(value = "beforeMessageId", required = false) Long beforeMessageId,
                                                               @Parameter(description = "本次最多返回消息数量，默认 30，最大 100")
                                                               @RequestParam(value = "limit", required = false) Integer limit)
    {
        return ApiResponse.success(conversationService.listMessages(token, conversationId, beforeMessageId, limit));
    }

    /**
     * 标记会话已读。
     *
     * @param token          登录 token
     * @param conversationId 会话 ID
     * @return 空响应
     */
    @Operation(summary = "标记会话已读", description = "将当前用户在该会话中的同步游标推进到最后一条消息。")
    @PostMapping("/{conversationId}/read")
    public ApiResponse<Void> markConversationRead(@Parameter(description = "登录 token")
                                                  @RequestHeader(value = "token", required = false) String token,
                                                  @Parameter(description = "会话ID")
                                                  @PathVariable Long conversationId)
    {
        conversationService.markConversationRead(token, conversationId);
        return ApiResponse.success(null);
    }
}
