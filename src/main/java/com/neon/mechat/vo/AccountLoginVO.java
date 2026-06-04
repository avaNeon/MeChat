package com.neon.mechat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLoginVO
{
    private String token;
    private AccountUserVO user;
}
