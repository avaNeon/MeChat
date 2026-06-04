package com.neon.mechat.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfflineMessageSyncVO
{
    private List<MessageVO> messages;
    private Boolean hasMore;
}
