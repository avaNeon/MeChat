package com.neon.mechat.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class FriendRequest
{
    private Long id;
    private Long requesterId;
    private Long addresseeId;
    private Long userLowId;
    private Long userHighId;
    private String requestMessage;
    private Integer status;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
