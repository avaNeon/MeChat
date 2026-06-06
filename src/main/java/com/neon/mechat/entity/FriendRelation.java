package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class FriendRelation
{
    private Long id;
    private Long userLowId;
    private Long userHighId;
    private Long initiatorId;
    private LocalDateTime createTime;
}
