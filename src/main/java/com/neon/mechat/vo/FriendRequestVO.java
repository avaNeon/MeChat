package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "好友申请信息")
public class FriendRequestVO
{
    @Schema(description = "好友申请ID")
    private Long id;

    @Schema(description = "申请发起人")
    private AccountUserVO requester;

    @Schema(description = "申请接收人")
    private AccountUserVO addressee;

    @Schema(description = "好友申请留言")
    private String message;

    @Schema(description = "申请状态：0 待处理，1 已同意，2 已拒绝")
    private Integer status;

    @Schema(description = "申请创建时间")
    private LocalDateTime createTime;
}
