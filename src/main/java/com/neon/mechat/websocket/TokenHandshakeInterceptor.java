package com.neon.mechat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class TokenHandshakeInterceptor implements HandshakeInterceptor
{
    private static final String TOKEN_KEY = "token";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes)
    {
        String token = request.getHeaders().getFirst(TOKEN_KEY);
        if (token != null)
        {
            attributes.put(TOKEN_KEY, token);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception)
    {
    }
}
