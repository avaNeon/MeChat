package com.neon.mechat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发送群聊消息参数")
public class SendGroupMessageDTO
{
    @Schema(description = "客户端生成的消息ID，用于发送幂等")
    private String clientMessageId;

    @Schema(description = "消息类型：1 文本，2 图片")
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    @Schema(description = "消息文本内容，图片消息可为空")
    @Size(max = 5000, message = "消息内容不能超过5000个字符")
    private String content;

    @Schema(description = "图片路径，通过上传图片接口获取")
    @Size(max = 512, message = "图片路径不能超过512个字符")
    private String imgPath;
}
