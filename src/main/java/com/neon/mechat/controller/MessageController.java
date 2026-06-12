package com.neon.mechat.controller;

import com.neon.mechat.common.ApiResponse;
import com.neon.mechat.dto.SendMessageDTO;
import com.neon.mechat.service.MessageService;
import com.neon.mechat.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "消息接口", description = "单聊消息发送；实时接收使用 WebSocket：/ws/messages")
public class MessageController
{
    private final MessageService messageService;

    @Operation(summary = "发送单聊消息", description = "发送者从请求头 token 中解析，clientMessageId 用于弱网重试时保证幂等。")
    @PostMapping("/send")
    public ApiResponse<MessageVO> sendMessage(@Parameter(description = "登录 token")
                                              @RequestHeader(value = "token", required = false) String token,
                                              @Valid @RequestBody SendMessageDTO sendMessageDTO)
    {
        return ApiResponse.success(messageService.sendMessage(token, sendMessageDTO));
    }
}
