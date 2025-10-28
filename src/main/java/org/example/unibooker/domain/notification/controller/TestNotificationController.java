package org.example.unibooker.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/notify/{userId}")
    public BaseResponse<String> sendTestNotification(@PathVariable Long userId,
                                                     @RequestParam String message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                new NotificationDto.notificationReq(message)
        );
        return BaseResponse.success("웹 소켓 메시지 전송 완료");
    }
}

