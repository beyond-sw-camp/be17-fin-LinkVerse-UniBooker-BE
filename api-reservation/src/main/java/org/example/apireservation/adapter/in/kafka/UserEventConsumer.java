package org.example.apireservation.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apireservation.adapter.out.UserRepository;
import org.example.apireservation.domain.model.entity.Users;
import org.example.common.event.UserSyncEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User 이벤트 Consumer
 * - api-app에서 발행한 User 생성/수정/삭제 이벤트를 수신
 * - api-reservation의 Users 테이블에 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * User 동기화 이벤트 수신
     */
    @KafkaListener(topics = "user-sync-events", groupId = "reservation-service-group")
    @Transactional
    public void consumeUserEvent(String message) {
        try {
            // 1. JSON 메시지 파싱
            UserSyncEvent event = objectMapper.readValue(message, UserSyncEvent.class);

            log.info("📩 User 이벤트 수신 - type: {}, userId: {}",
                    event.getEventType(), event.getUserId());

            // 2. 이벤트 타입별 처리
            switch (event.getEventType()) {
                case CREATED -> handleUserCreated(event);
                case UPDATED -> handleUserUpdated(event);
                case DELETED -> handleUserDeleted(event);
                default -> log.warn("⚠️ 알 수 없는 이벤트 타입: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("❌ User 이벤트 처리 실패", e);
        }
    }

    /**
     * User 생성 이벤트 처리
     */
    private void handleUserCreated(UserSyncEvent event) {
        Optional<Users> existingUser = userRepository.findById(event.getUserId());

        if (existingUser.isPresent()) {
            log.warn("⚠️ 이미 존재하는 User - userId: {}, 업데이트로 처리", event.getUserId());
            handleUserUpdated(event);
            return;
        }

        Users user = Users.builder()
                .id(event.getUserId())
                .email(event.getEmail())
                .password(event.getPassword())
                .name(event.getName())
                .phone(event.getPhone())
                .birthDate(event.getBirthDate())
                .gender(event.getGender())
                .companyId(event.getCompanyId())
                .role(event.getRole())
                .status(event.getStatus())
                .isFirstLogin(event.getIsFirstLogin())
                .suspendedByCompany(event.getSuspendedByCompany())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .deletedAt(null)
                .build();

        userRepository.save(user);
        log.info("✅ User 생성 완료 - userId: {}, email: {}", user.getId(), user.getEmail());
    }

    /**
     * User 수정 이벤트 처리
     */
    private void handleUserUpdated(UserSyncEvent event) {
        Users user = userRepository.findById(event.getUserId())
                .orElseGet(() -> {
                    log.warn("⚠️ User가 존재하지 않음, 새로 생성 - userId: {}", event.getUserId());
                    return Users.builder()
                            .id(event.getUserId())
                            .build();
                });

        user.updateFromEvent(
                event.getEmail(),
                event.getPassword(),
                event.getName(),
                event.getPhone(),
                event.getBirthDate(),
                event.getGender(),
                event.getCompanyId(),
                event.getRole(),
                event.getStatus(),
                event.getIsFirstLogin(),
                event.getSuspendedByCompany(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );

        userRepository.save(user);
        log.info("✅ User 수정 완료 - userId: {}, email: {}", user.getId(), user.getEmail());
    }

    /**
     * User 삭제 이벤트 처리 (Soft Delete)
     */
    private void handleUserDeleted(UserSyncEvent event) {
        Optional<Users> userOptional = userRepository.findById(event.getUserId());

        if (userOptional.isEmpty()) {
            log.warn("⚠️ 삭제할 User가 존재하지 않음 - userId: {}", event.getUserId());
            return;
        }

        Users user = userOptional.get();
        user.delete(event.getDeletedAt());

        userRepository.save(user);
        log.info("✅ User 삭제 완료 - userId: {}", event.getUserId());
    }
}