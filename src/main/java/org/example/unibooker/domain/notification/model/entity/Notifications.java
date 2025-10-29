package org.example.unibooker.domain.notification.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.notification.model.NotificationType;
import org.example.unibooker.domain.notification.model.NotificationStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * - ERD 기반 notifications 테이블 매핑
 * - 모든 권한(SUPER, ADMIN, USER)의 알림을 통합 관리
 * - BaseEntity 상속으로 created_at, updated_at, deleted_at 자동 관리
 */
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Notifications extends BaseEntity {

    @Comment("사용자 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @Comment("카테고리")
    private NotificationType category;

    @Comment("알림 제목")
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    @Comment("알림 메시지")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Comment("발송 상태")
    @Setter
    private NotificationStatus status;

    @Column(name = "is_read", nullable = false)
    @Comment("읽음 여부")
    @Setter
    private Boolean isRead = false;

    @Column(name = "read_at")
    @Comment("읽은 시각")
    @Setter
    private LocalDateTime readAt;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    @Comment("실패 사유")
    @Setter
    private String failedReason;

    @Comment("재시도 횟수")
    @Setter
    private Integer retryCount = 0;

}