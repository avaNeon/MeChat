package com.neon.mechat.websocket;

import com.neon.mechat.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler
{
    private static final String TOKEN_KEY = "token";
    private static final String USER_ID_KEY = "userId";

    private final AccountRepository accountRepository;
    private final OnlineSessionManager onlineSessionManager;

    /**
     * WebSocket 握手成功后校验 token，并将用户 ID 和连接绑定到在线连接管理器。
     *
     * @param session 新建立的 WebSocket 连接
     * @throws Exception 关闭连接时可能抛出的底层异常
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        String token = getToken(session);
        if (!StringUtils.hasText(token))
        {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未登录"));
            return;
        }

        Long userId = accountRepository.findUserIdByToken(token);
        if (userId == null)
        {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("登录已失效"));
            return;
        }

        session.getAttributes().put(USER_ID_KEY, userId);
        onlineSessionManager.addSession(userId, session);
    }

    /**
     * 处理客户端主动发送的文本消息，目前只支持 ping/pong 心跳。
     *
     * @param session WebSocket 连接
     * @param message 客户端文本消息
     * @throws Exception 发送 pong 失败时可能抛出的底层异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
    {
        if ("ping".equalsIgnoreCase(message.getPayload()))
        {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    /**
     * 连接正常关闭后清理在线连接，避免后续推送命中失效 session。
     *
     * @param session 已关闭的 WebSocket 连接
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        removeSession(session);
    }

    /**
     * 连接传输异常时清理在线连接，避免异常连接长期占用内存。
     *
     * @param session 发生异常的 WebSocket 连接
     * @param exception 传输异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
    {
        removeSession(session);
    }

    /**
     * 从 WebSocket 连接中提取 token。优先级：握手拦截器注入的 attributes > 握手请求头 > URI 查询参数。
     *
     * @param session WebSocket 连接
     * @return token，不存在时返回 null
     */
    private String getToken(WebSocketSession session)
    {
        Object attrToken = session.getAttributes().get(TOKEN_KEY);
        if (attrToken instanceof String token && StringUtils.hasText(token))
        {
            return token;
        }
        String headerToken = session.getHandshakeHeaders().getFirst(TOKEN_KEY);
        if (StringUtils.hasText(headerToken))
        {
            return headerToken;
        }
        return getTokenFromQuery(session.getUri());
    }

    /**
     * 从连接 URI 查询参数中读取 token。
     *
     * @param uri WebSocket 连接 URI
     * @return token，不存在时返回 null
     */
    private String getTokenFromQuery(URI uri)
    {
        if (uri == null)
        {
            return null;
        }
        return UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst(TOKEN_KEY);
    }

    /**
     * 根据 session 中缓存的 userId 移除在线连接。
     *
     * @param session 需要清理的 WebSocket 连接
     */
    private void removeSession(WebSocketSession session)
    {
        Object userId = session.getAttributes().get(USER_ID_KEY);
        if (userId instanceof Long currentUserId)
        {
            onlineSessionManager.removeSession(currentUserId, session);
        }
    }
}
