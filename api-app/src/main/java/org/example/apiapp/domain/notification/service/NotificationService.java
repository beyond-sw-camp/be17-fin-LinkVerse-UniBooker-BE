package org.example.apiapp.domain.notification.service;

import org.example.apiapp.domain.notification.model.NotificationType;
import org.example.apiapp.domain.notification.model.NotificationStatus;
import org.example.apiapp.domain.notification.model.dto.NotificationDto;
import org.example.apiapp.domain.notification.model.entity.Notifications;
import org.example.apiapp.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.user.repository.UserRepository;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;

/**
 * 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;



    // -------------------- 알림 대상 타입 조회 --------------------
    @Transactional
    public void sendNotificationToRole(NotificationType type, UserRole role, Object... args) {
        // 1️⃣ 해당 역할 유저 전부 조회
        List<Users> users = userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE, Pageable.unpaged()).getContent();

        // 2️⃣ 각 유저에게 알림 생성 및 전송
        for (Users user : users) {
            sendNotificationToUser(type, user, args);
        }
    }


    // -------------------- 특정 대상에게 알림 전송 --------------------
    @Transactional
    public void sendNotificationToUser(NotificationType type, Users targetUser, Object... args) {
        String title;
        String message;

        try {
            // 1️⃣ args 개수에 따라 %s 자동 포맷 적용
            if (args != null && args.length > 0) {
                title = String.format(type.getTitle(), args);
                message = String.format(type.getMessage(), args);
            } else {
                title = type.getTitle();
                message = type.getMessage();
            }
        } catch (MissingFormatArgumentException e) {
            // %s 개수 안 맞을 때 fallback
            title = type.getTitle();
            message = type.getMessage();
        }

        // 2️⃣ DTO 생성
        NotificationDto.NotificationReq req = NotificationDto.NotificationReq.builder()
                .category(type)
                .title(title)
                .message(message)
                .build();

        // 3️⃣ 엔티티 변환 및 저장
        Notifications notification = req.toEntity(targetUser);
        notificationRepository.save(notification);

        try {
            // 4️⃣ WebSocket 실시간 전송
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(targetUser.getId()),
                    "/queue/notifications",
                    Map.of(
                            "title", title,
                            "message", message,
                            "category", req.getCategory().name()
                    )
            );

            // 5️⃣ 상태 업데이트
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            log.info("✅ 알림 전송 성공 - userId: {}, type: {}, title: {}, message: {}", targetUser.getId(), type, title, message);

        } catch (Exception e) {
            // 6️⃣ 실패 처리
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.error("❌ 알림 전송 실패 - userId: {}, type: {}, reason: {}", targetUser.getId(), type, e.getMessage());
        }
    }


    /**
     * 사용자 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Notifications> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notifications notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}