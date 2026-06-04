package com.neon.mechat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUserVO
{
    private Long userId;
    private String nickname;
    private String avatar;
}
