package org.example.unibooker.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String jwt = extractTokenFromCookie(httpRequest);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                Long userId = jwtUtil.getUserId(jwt);
                attributes.put("userId", userId);

                // ✅ Principal 등록
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userId, // principal
                        null,
                        List.of() // 권한 없으면 빈 리스트
                );
                attributes.put("SPRING_SECURITY_CONTEXT", auth);

                log.info("✅ WebSocket 인증 성공 - userId={}", userId);
                return true;
            } else {
                log.warn("❌ WebSocket 인증 실패 - JWT 없음 또는 유효하지 않음");
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // nothing
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            String name = cookie.getName();

            // accessToken으로 끝나는 모든 쿠키 허용
            if (name.endsWith("AccessToken")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
