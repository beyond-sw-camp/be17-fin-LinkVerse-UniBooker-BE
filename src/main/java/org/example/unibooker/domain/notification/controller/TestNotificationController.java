package org.example.unibooker.domain.notification.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Hidden
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    private MessageHeaders createHeaders(@Nullable String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        if (sessionId != null) accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor.getMessageHeaders();
    }

    @GetMapping("/notify/{userId}")
    public void sendNotification(@PathVariable Long userId) {
        Map<String, Object> data = Map.of(
                "title", "테스트 알림",
                "message", "WebSocket 연결이 잘 됩니다 🎉"
        );
            messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                data
        );
    }

    @GetMapping("/send")
    public void send() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = "웹소켓 알림입니다!";
        messagingTemplate.convertAndSend("/user/queue/notifications", "[" + now + "] " + message);
    }
}

