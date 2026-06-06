package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.service.MessageService;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.vo.OfflineMessageSyncVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "消息接口", description = "单聊消息发送、离线同步；实时接收使用 WebSocket：/ws/messages?token={token}")
public class MessageController
{
    private final MessageService messageService;

    /**
     * 发送单聊消息。
     *
     * @param token          登录后返回的 token，用于识别当前发送者
     * @param sendMessageDTO 发送消息参数，包含接收者、客户端消息ID、消息类型和内容
     * @return 已落库的消息信息
     */
    @Operation(summary = "发送单聊消息", description = "发送者从请求头 token 中解析，客户端不需要也不能传 senderId。clientMessageId 用于弱网重试时保证幂等。")
    @PostMapping("/send")
    public ApiResponse<MessageVO> sendMessage(@Parameter(description = "登录 token")
                                              @RequestHeader(value = "token", required = false) String token,
                                              @Valid @RequestBody SendMessageDTO sendMessageDTO)
    {
        return ApiResponse.success(messageService.sendMessage(token, sendMessageDTO));
    }

    /**
     * 同步当前用户尚未拉取的离线消息。
     *
     * @param token 登录 token，用于识别当前同步用户
     * @param limit 本次最多返回的消息数量
     * @return 离线消息列表和是否还有更多数据
     */
    @Operation(summary = "同步离线消息", description = "按当前用户在会话中的最后同步消息ID拉取未同步消息。返回成功后，服务端会推进对应会话的同步游标。")
    @PostMapping("/offline-sync")
    public ApiResponse<OfflineMessageSyncVO> syncOfflineMessages(@Parameter(description = "登录 token")
                                                                 @RequestHeader(value = "token", required = false) String token,
                                                                 @Parameter(description = "本次最多返回消息数量，默认 50，最大 100", example = "50")
                                                                 @RequestParam(value = "limit", defaultValue = "50") Integer limit)
    {
        return ApiResponse.success(messageService.syncOfflineMessages(token, limit));
    }
}
