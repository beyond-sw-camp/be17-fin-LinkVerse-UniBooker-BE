package org.example.apimain.domain.notification.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 연결 관리 핸들러
 * - WebSocket 연결/종료 시 로깅 및 검증
 * - 실제 메시지 전송은 NotificationService에서 STOMP로 처리
 */
@Slf4j
@Component
public class NotificationHandler extends TextWebSocketHandler {

    /**
     * WebSocket 연결 성공 시
     * - JWT에서 추출한 userId 검증
     * - 인증 정보 없으면 연결 종료
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");

        if (userId != null) {
            log.info("✅ WebSocket 연결 성공 - userId: {}, sessionId: {}",
                    userId, session.getId());
        } else {
            log.warn("❌ WebSocket 인증 정보 없음 - sessionId: {}", session.getId());
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("인증 정보가 없습니다."));
        }
    }

    /**
     * WebSocket 연결 종료 시
     * - 연결 종료 로깅
     * - 필요시 세션 정리 (현재는 로깅만)
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("🔴 WebSocket 연결 종료 - userId: {}, sessionId: {}, status: {}",
                userId, session.getId(), status);
    }

    /**
     * 에러 발생 시
     * - 에러 로깅 및 세션 종료
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("❌ WebSocket 에러 발생 - userId: {}, sessionId: {}, error: {}",
                userId, session.getId(), exception.getMessage());

        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (Exception e) {
            log.error("세션 종료 실패: {}", e.getMessage());
        }
    }
}