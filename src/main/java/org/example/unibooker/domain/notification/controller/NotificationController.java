package org.example.unibooker.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 API 컨트롤러
 * - ERD notifications 테이블 기반
 * - 모든 권한(SUPER, ADMIN, USER)이 본인의 알림만 조회/관리 가능
 */
@Tag(name = "Notification API", description = "알림 관리 API")
@RestController
@RequestMapping("/api/ub")
@RequiredArgsConstructor
public class NotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 "/pub/message"로 보낸 메시지를 처리
    @MessageMapping("/message")
    public void handleMessage(NotificationDto.notificationReq message) {
        // 받은 메시지를 /sub/notifications 구독자에게 전달
        messagingTemplate.convertAndSend("/sub/notifications", message);
    }

    @MessageMapping("/hello")
    public void handleHelloMessage(String message) {
        // 로그인한 사용자에게만 알림 보내기 (예시)
        String userId = "1"; // 실제는 JWT 기반으로 사용자 식별 필요
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", "Welcome, " + message + "!");
    }
}