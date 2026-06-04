package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "离线消息同步结果")
public class OfflineMessageSyncVO
{
    @Schema(description = "本次同步到的离线消息")
    private List<MessageVO> messages;

    @Schema(description = "是否还有更多离线消息", example = "false")
    private Boolean hasMore;
}
