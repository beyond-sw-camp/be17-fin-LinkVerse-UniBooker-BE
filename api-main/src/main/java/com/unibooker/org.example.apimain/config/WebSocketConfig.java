package com.unibooker.main.config;

import com.unibooker.common.util.JwtUtil;
import com.unibooker.main.utils.JwtHandshakeHandler;
import com.unibooker.main.utils.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * - STOMP 프로토콜 사용
 * - SockJS fallback 지원
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil))
                .setHandshakeHandler(new JwtHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 → 서버
        registry.setApplicationDestinationPrefixes("/pub");

        // 서버 → 클라이언트
        registry.enableSimpleBroker("/sub", "/queue", "/topic");
        registry.setUserDestinationPrefix("/user");
    }
}