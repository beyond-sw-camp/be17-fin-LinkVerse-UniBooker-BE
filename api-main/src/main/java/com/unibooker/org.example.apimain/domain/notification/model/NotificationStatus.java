package com.unibooker.main.domain.notification.model;

/**
 * 알림 발송 상태
 */
public enum NotificationStatus {
    PENDING,  // 발송 대기
    SENT,     // 발송 완료
    FAILED    // 발송 실패
}