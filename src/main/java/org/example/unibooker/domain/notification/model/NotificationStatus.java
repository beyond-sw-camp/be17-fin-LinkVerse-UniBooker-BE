package org.example.unibooker.domain.notification.model;

import lombok.Getter;

/**
 * 알림 발송 상태 (ERD 기반)
 * - PENDING: 발송 대기
 * - SENT: 발송 완료
 * - FAILED: 발송 실패
 */
@Getter
public enum NotificationStatus {
    PENDING("발송 대기", "Pending"),
    SENT("발송 완료", "Sent"),
    FAILED("발송 실패", "Failed");

    private final String koreanName;
    private final String englishName;

    NotificationStatus(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    /**
     * 언어에 따른 상태명 반환
     */
    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return koreanName;
        }
        return englishName;
    }
}