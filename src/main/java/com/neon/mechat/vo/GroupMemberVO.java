package com.neon.mechat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "群成员信息")
public class GroupMemberVO
{
    @Schema(description = "用户信息")
    private AccountUserVO user;

    @Schema(description = "角色：0 普通成员，1 群主")
    private Integer role;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;
}
