package org.example.unibooker.domain.notification.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.notification.model.NotificationCategory;
import org.example.unibooker.domain.notification.model.NotificationStatus;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 알림 엔티티
 * - ERD 기반 notifications 테이블 매핑
 * - 모든 권한(SUPER, ADMIN, USER)의 알림을 통합 관리
 * - BaseEntity 상속으로 created_at, updated_at, deleted_at 자동 관리
 */
@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_user_read", columnList = "user_id, is_read"),
                @Index(name = "idx_notification_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("알림")
public class Notifications extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @Comment("카테고리")
    private NotificationCategory category;

    @Column(name = "message", nullable = false, length = 500)
    @Comment("알림 메시지")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Comment("발송 상태")
    private NotificationStatus status;

    @Column(name = "is_read", nullable = false)
    @Comment("읽음 여부")
    private Boolean isRead;

    @Column(name = "read_at")
    @Comment("읽은 시각")
    private LocalDateTime readAt;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    @Comment("실패 사유")
    private String failedReason;

    @Builder
    public Notifications(Long userId, NotificationCategory category, String message,
                         NotificationStatus status, Boolean isRead) {
        this.userId = userId;
        this.category = category;
        this.message = message;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.isRead = isRead != null ? isRead : false;
    }

    // ========== 읽음 처리 ==========

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 알림을 안읽음 상태로 변경
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    // ========== 발송 상태 관리 ==========

    /**
     * 발송 완료 상태로 변경
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    /**
     * 발송 실패 상태로 변경
     */
    public void markAsFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failedReason = reason;
    }

    /**
     * 발송 대기 상태로 초기화 (재발송용)
     */
    public void resetToPending() {
        this.status = NotificationStatus.PENDING;
        this.failedReason = null;
    }

    // ========== 삭제 관리 ==========

    /**
     * 소프트 삭제 처리
     */
    public void softDelete() {
        this.setDeletedAt(LocalDateTime.now());
    }

    /**
     * 삭제 취소 (복구)
     */
    public void restore() {
        this.setDeletedAt(null);
    }

    // ========== 상태 확인 ==========

    /**
     * 읽음 상태 확인
     */
    public boolean isRead() {
        return this.isRead;
    }

    /**
     * 안읽음 상태 확인
     */
    public boolean isUnread() {
        return !this.isRead;
    }

    /**
     * 발송 대기 상태 확인
     */
    public boolean isPending() {
        return this.status == NotificationStatus.PENDING;
    }

    /**
     * 발송 완료 상태 확인
     */
    public boolean isSent() {
        return this.status == NotificationStatus.SENT;
    }

    /**
     * 발송 실패 상태 확인
     */
    public boolean isFailed() {
        return this.status == NotificationStatus.FAILED;
    }

    /**
     * 삭제 상태 확인
     */
    public boolean isDeleted() {
        return this.getDeletedAt() != null;
    }

    // ========== 권한 검증 ==========

    /**
     * 특정 사용자의 알림인지 확인
     */
    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    /**
     * 읽음 처리 가능 여부 확인
     */
    public boolean canMarkAsRead() {
        return !this.isRead && !this.isDeleted();
    }

    /**
     * 재발송 가능 여부 확인
     */
    public boolean canResend() {
        return this.isFailed() && !this.isDeleted();
    }

    // ========== 메시지 수정 ==========

    /**
     * 알림 메시지 수정 (발송 전에만 가능)
     */
    public void updateMessage(String newMessage) {
        if (this.isPending() && newMessage != null && !newMessage.isBlank()) {
            this.message = newMessage;
        }
    }
}