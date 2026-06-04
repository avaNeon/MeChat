package com.neon.mechat.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineSessionManager
{
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    /**
     * 记录用户的一条在线 WebSocket 连接，同一用户允许多端同时在线。
     *
     * @param userId 用户 ID
     * @param session WebSocket 连接
     */
    public void addSession(Long userId, WebSocketSession session)
    {
        userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * 获取用户当前所有在线连接，用于实时消息推送到多端。
     *
     * @param userId 用户 ID
     * @return 在线连接集合，用户离线时返回空集合
     */
    public Set<WebSocketSession> getSessions(Long userId)
    {
        return userSessions.getOrDefault(userId, Set.of());
    }

    /**
     * 移除用户的一条在线 WebSocket 连接。
     *
     * @param userId 用户 ID
     * @param session 需要移除的 WebSocket 连接
     */
    public void removeSession(Long userId, WebSocketSession session)
    {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null)
        {
            return;
        }

        sessions.remove(session);
        if (sessions.isEmpty())
        {
            // 用户没有剩余在线连接时，清理用户在线记录。
            userSessions.remove(userId);
        }
    }
}
