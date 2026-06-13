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

    @Operation(summary = "上传聊天图片", description = "上传聊天图片，返回相对于 storage 目录的路径，后续发送消息时传入 imgPath。")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            @Parameter(description = "图片文件，支持 jpg/jpeg/png/gif/webp")
            @RequestPart("imageFile") MultipartFile imageFile)
    {
        return ApiResponse.success(messageService.uploadImage(token, imageFile));
    }

    @Operation(summary = "下载聊天图片", description = "根据上传图片接口返回的路径下载图片。")
    @GetMapping("/images/**")
    public ResponseEntity<Resource> downloadImage(
            @Parameter(description = "登录 token") @RequestHeader(value = "token", required = false) String token,
            jakarta.servlet.http.HttpServletRequest request)
    {
        String requestUri = request.getRequestURI();
        String relativePath = requestUri.substring(requestUri.indexOf("/images/") + "/images/".length());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(messageService.getImageContentType(relativePath)))
                .body(messageService.downloadImage(token, relativePath));
    }
}
