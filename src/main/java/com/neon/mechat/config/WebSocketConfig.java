package com.neon.mechat.config;

import com.neon.mechat.websocket.MessageWebSocketHandler;
import com.neon.mechat.websocket.TokenHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

    @Value("${mechat.websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .addInterceptors(tokenHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
