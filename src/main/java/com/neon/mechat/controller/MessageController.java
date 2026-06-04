package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.service.MessageService;
import com.neon.mechat.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
}
