package org.example.unibooker.config;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.utils.JwtHandshakeHandler;
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
                .setHandshakeHandler(new JwtHandshakeHandler())
                .withSockJS(); // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 → 서버
        registry.setApplicationDestinationPrefixes("/pub");

        // 서버 → 클라이언트
        registry.enableSimpleBroker("/sub", "/queue", "/topic", "/user");
        registry.setUserDestinationPrefix("/user"); // 특정 유저에게 전송


//         /sub/** → 일반 브로드캐스트용 토픽. 누구든 구독하면 다 받는 토픽(ex. 사이트 전체 공지)
//         /topic/** → 주로 다중 사용자 브로드캐스트용. 채팅방처럼 방 단위로 다수에게 보내는 용도(ex. 채팅방 알림)
//         /queue/** → 주로 1:1 큐용. 경로 정확히 맞춰야 메시지 수신(convertAndSend("/queue/notifications/5", msg))
//         /user/** → convertAndSendToUser() 사용 시 1:1 사용자 전용. 서버가 userId ↔ 세션 자동 매핑, 브라우저 경로 고정 (convertAndSendToUser("5", "/queue/notifications", msg))
    }
}