package org.example.unibooker.config;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.utils.JwtHandshakeInterceptor;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


/**
 소켓 서비의 설정 파일입니다.
 - registerStompEndpoints() : 클라이언트가 최초로 연결할 엔드포인트(URL) 등록
   -> 예 : new SockJS("/ws")
 - configureMessageBroker() : 메시지의 흐름 방향(prefix) 설정
   - /pub/** : 클라이언트 -> 서버
   - /sub/**, /user/** : 서버 -> 클라이언트

 즉, 사용자는 /pub/** 으로 메시지를 보내고,
 서버는 /sub/**, /user/** 로 메시지를 보냅니다.
 */


@Configuration
@EnableWebSocketMessageBroker // STOMP 기반 WebSocket 사용
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 연결할 WebSocket 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용
                .addInterceptors(new HttpSessionHandshakeInterceptor(), new JwtHandshakeInterceptor(jwtUtil))
                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 → 서버
        registry.setApplicationDestinationPrefixes("/pub");

        // 서버 → 클라이언트
        registry.enableSimpleBroker("/sub", "/queue", "/topic", "/user");
        registry.setUserDestinationPrefix("/user"); // 특정 유저에게 전송
    }
}