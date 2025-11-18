package org.example.apiapp.domain.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.apiapp.domain.notification.model.NotificationType;
import org.example.apiapp.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.user.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka 알림 Consumer
 * - reservation-completed 토픽에서 예약 완료 이벤트 수신
 * - 사용자에게 실시간 알림 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    /**
     * 예약 완료 이벤트 수신
     */
    @KafkaListener(topics = "reservation-completed", groupId = "main-service-group")
    public void handleReservationCompleted(String message) {
        try {
            log.info("📨 Kafka 메시지 수신 - topic: reservation-completed, message: {}", message);

            // JSON 파싱
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long userId = ((Number) event.get("userId")).longValue();
            String resourceName = (String) event.get("resourceName");

            // DB에서 사용자 조회
            Users targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 알림 전송
            String notificationMessage = String.format("'%s' 예약이 완료되었습니다.", resourceName);
            notificationService.sendNotificationToUser(
                    NotificationType.RESERVATION_CONFIRMED,
                    targetUser,
                    resourceName
            );

            log.info("✅ 예약 완료 알림 전송 완료 - userId: {}", userId);

        } catch (Exception e) {
            log.error("❌ Kafka 메시지 처리 실패", e);
        }
    }
}