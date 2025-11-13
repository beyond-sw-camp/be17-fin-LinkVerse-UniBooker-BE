package com.unibooker.main.domain.notification.service;

import com.unibooker.main.domain.notification.model.NotificationType;
import com.unibooker.main.domain.notification.model.NotificationStatus;
import com.unibooker.main.domain.notification.model.entity.Notifications;
import com.unibooker.main.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    /**
     * 특정 사용자에게 알림 전송
     */
    @Transactional
    public void sendNotificationToUser(Long userId, NotificationType type, String message) {
        String title = type.getTitle();

        // 알림 생성
        Notifications notification = Notifications.builder()
                .userId(userId)
                .category(type)
                .title(title)
                .message(message)
                .status(NotificationStatus.PENDING)
                .isRead(false)
                .retryCount(0)
                .build();

        notificationRepository.save(notification);

        try {
            // WebSocket으로 실시간 전송
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/notifications",
                    Map.of(
                            "title", title,
                            "message", message,
                            "category", type.name()
                    )
            );

            // 상태 업데이트
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            log.info("✅ 알림 전송 성공 - userId: {}, type: {}", userId, type);

        } catch (Exception e) {
            // 실패 처리
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.error("❌ 알림 전송 실패 - userId: {}, type: {}", userId, type, e);
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