package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.service.MessageService;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.vo.OfflineMessageSyncVO;
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
    @PostMapping("/send")
    public ApiResponse<MessageVO> sendMessage(@RequestHeader(value = "token", required = false) String token,
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
    @PostMapping("/offline-sync")
    public ApiResponse<OfflineMessageSyncVO> syncOfflineMessages(@RequestHeader(value = "token", required = false) String token,
                                                                 @RequestParam(value = "limit", defaultValue = "50") Integer limit)
    {
        return ApiResponse.success(messageService.syncOfflineMessages(token, limit));
    }
}
