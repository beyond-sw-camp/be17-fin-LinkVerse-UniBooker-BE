package com.unibooker.main.domain.notification.model.entity;

import com.unibooker.common.entity.BaseEntity;
import com.unibooker.main.domain.notification.model.NotificationType;
import com.unibooker.main.domain.notification.model.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notifications extends BaseEntity {

    /** 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 알림 카테고리 */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private NotificationType category;

    /** 알림 제목 */
    @Column(name = "title", length = 255)
    private String title;

    /** 알림 메시지 */
    @Column(name = "message", nullable = false, length = 500)
    private String message;

    /** 발송 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    /** 읽음 여부 */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /** 읽은 시각 */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /** 실패 사유 */
    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason;

    /** 재시도 횟수 */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;
}