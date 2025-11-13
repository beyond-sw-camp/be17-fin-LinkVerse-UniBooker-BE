package com.unibooker.main.utils;

import com.unibooker.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Handshake 인터셉터
 * - JWT 토큰 검증
 * - userId 추출하여 세션에 저장
 */
@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String query = request.getURI().getQuery();

        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1].split("&")[0];

            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserId(token);
                attributes.put("userId", userId);
                log.info("✅ WebSocket 인증 성공 - userId: {}", userId);
                return true;
            }
        }

        log.warn("❌ WebSocket 인증 실패");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 후처리 로직 (필요시 구현)
    }
}