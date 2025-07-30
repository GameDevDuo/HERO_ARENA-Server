package com.gamedevduo.heroarena.domain.game;

import com.gamedevduo.heroarena.global.security.jwt.JwtProvider;
import com.gamedevduo.heroarena.global.security.jwt.enums.JwtType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        List<String> authHeaders = request.getHeaders().get("Authorization");

        String token = jwtProvider.resolveToken(authHeaders.get(0));
        if (token == null || !jwtProvider.validateToken(token, JwtType.ACCESS_TOKEN)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String userId = jwtProvider.getPayload(token, JwtType.ACCESS_TOKEN).getSubject();
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}