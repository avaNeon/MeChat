package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话历史消息分页结果")
public class ConversationMessagePageVO
{
    @Schema(description = "消息列表，按发送时间正序排列")
    private List<MessageVO> messages;

    @Schema(description = "是否还有更早的消息")
    private Boolean hasMore;
}
