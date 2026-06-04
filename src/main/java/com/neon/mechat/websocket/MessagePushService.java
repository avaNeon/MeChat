package com.neon.mechat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.mechat.vo.MessageVO;
import com.neon.mechat.vo.WebSocketMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePushService
{
    private static final String MESSAGE_TYPE = "message";

    private final OnlineSessionManager onlineSessionManager;
    private final ObjectMapper objectMapper;

    /**
     * 将消息推送给接收者当前所有在线 WebSocket 连接。
     *
     * @param messageVO 已持久化的消息
     */
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

    /**
     * 将消息 VO 包装成 WebSocket 统一消息结构，客户端通过 type 区分事件类型。
     *
     * @param messageVO 消息 VO
     * @return JSON 字符串
     * @throws JsonProcessingException JSON 序列化失败时抛出
     */
    private String toPayload(MessageVO messageVO) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(new WebSocketMessageVO<>(MESSAGE_TYPE, messageVO));
    }

    /**
     * 向单条 WebSocket 连接发送文本消息。
     *
     * @param userId 接收者用户 ID
     * @param session WebSocket 连接
     * @param payload 已序列化的消息内容
     */
    private void sendMessage(Long userId, WebSocketSession session, String payload)
    {
        if (!session.isOpen())
        {
            // 连接已关闭时直接清理，避免后续推送继续命中失效 session。
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
            // 推送失败不能影响消息落库结果，只清理连接并记录日志。
            onlineSessionManager.removeSession(userId, session);
            log.warn("Send websocket message failed, userId={}", userId, exception);
        }
    }
}
