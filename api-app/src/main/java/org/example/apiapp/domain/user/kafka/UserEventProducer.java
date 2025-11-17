package org.example.apiapp.domain.user.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.common.event.UserSyncEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * User 이벤트 Producer
 * - User 생성/수정/삭제 이벤트를 Kafka로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Kafka Topic 이름
     */
    private static final String TOPIC = "user-sync-events";

    /**
     * User 생성 이벤트 발행
     */
    public void publishUserCreated(Users user) {
        try {
            UserSyncEvent event = UserSyncEvent.fromUserCreated(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getName(),
                    user.getPhone(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getCompanyId(),
                    user.getRole(),
                    user.getStatus(),
                    user.getIsFirstLogin(),
                    user.getSuspendedByCompany(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, String.valueOf(user.getId()), message);

            log.info("✅ User 생성 이벤트 발행 - userId: {}, email: {}", user.getId(), user.getEmail());

        } catch (JsonProcessingException e) {
            log.error("❌ User 생성 이벤트 발행 실패 - userId: {}", user.getId(), e);
        }
    }

    /**
     * User 수정 이벤트 발행
     */
    public void publishUserUpdated(Users user) {
        try {
            UserSyncEvent event = UserSyncEvent.fromUserUpdated(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getName(),
                    user.getPhone(),
                    user.getBirthDate(),
                    user.getGender(),
                    user.getCompanyId(),
                    user.getRole(),
                    user.getStatus(),
                    user.getIsFirstLogin(),
                    user.getSuspendedByCompany(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, String.valueOf(user.getId()), message);

            log.info("✅ User 수정 이벤트 발행 - userId: {}, email: {}", user.getId(), user.getEmail());

        } catch (JsonProcessingException e) {
            log.error("❌ User 수정 이벤트 발행 실패 - userId: {}", user.getId(), e);
        }
    }

    /**
     * User 삭제 이벤트 발행 (Soft Delete)
     */
    public void publishUserDeleted(Long userId, java.time.LocalDateTime deletedAt) {
        try {
            UserSyncEvent event = UserSyncEvent.fromUserDeleted(userId, deletedAt);

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, String.valueOf(userId), message);

            log.info("✅ User 삭제 이벤트 발행 - userId: {}", userId);

        } catch (JsonProcessingException e) {
            log.error("❌ User 삭제 이벤트 발행 실패 - userId: {}", userId, e);
        }
    }
}