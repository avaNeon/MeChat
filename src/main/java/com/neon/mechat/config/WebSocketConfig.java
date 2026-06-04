package com.neon.mechat.config;

import com.neon.mechat.websocket.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer
{
    private final MessageWebSocketHandler messageWebSocketHandler;

    /**
     * 注册消息实时接收 WebSocket 入口，客户端连接后由 handler 完成 token 鉴权和在线会话绑定。
     *
     * @param registry WebSocket handler 注册器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*");
    }
}
