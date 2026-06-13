package com.neon.mechat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.mechat.mapper.GroupChatMapper;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.vo.WebSocketMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePushService
{
    private static final String MESSAGE_TYPE = "message";

    private final OnlineSessionManager onlineSessionManager;
    private final GroupChatMapper groupChatMapper;
    private final ObjectMapper objectMapper;

    public void pushMessage(MessageVO messageVO)
    {
        String payload;
        try
        {
            payload = toPayload(messageVO);
        }
        catch (JsonProcessingException exception)
        {
            log.warn("Serialize websocket message failed, messageId={}", messageVO.getId(), exception);
            return;
        }

        for (WebSocketSession session : onlineSessionManager.getSessions(messageVO.getReceiverId()))
        {
            sendMessage(messageVO.getReceiverId(), session, payload);
        }
    }

    public void pushGroupMessage(MessageVO messageVO)
    {
        String payload;
        try
        {
            payload = toPayload(messageVO);
        }
        catch (JsonProcessingException exception)
        {
            log.warn("Serialize websocket message failed, messageId={}", messageVO.getId(), exception);
            return;
        }

        List<Long> memberIds = groupChatMapper.selectMemberIds(messageVO.getGroupId());
        for (Long memberId : memberIds)
        {
            if (memberId.equals(messageVO.getSenderId()))
            {
                continue;
            }
            for (WebSocketSession session : onlineSessionManager.getSessions(memberId))
            {
                sendMessage(memberId, session, payload);
            }
        }
    }

    private String toPayload(MessageVO messageVO) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new WebSocketMessageVO<>(MESSAGE_TYPE, messageVO));
    }

    private void sendMessage(Long userId, WebSocketSession session, String payload)
    {
        if (!session.isOpen())
        {
            onlineSessionManager.removeSession(userId, session);
            return;
        }

        try
        {
            synchronized (session)
            {
                session.sendMessage(new TextMessage(payload));
            }
        }
        catch (IOException exception)
        {
            onlineSessionManager.removeSession(userId, session);
            log.warn("Send websocket message failed, userId={}", userId, exception);
        }
    }
}
