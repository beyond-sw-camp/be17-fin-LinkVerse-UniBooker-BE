package com.unibooker.main.domain.notification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 예약 관련
    RESERVATION_CONFIRMED("예약 확인", "예약이 확정되었습니다."),
    RESERVATION_CANCELLED("예약 취소", "예약이 취소되었습니다."),

    // 관리자 관련
    COMPANY_APPROVED("기업 승인", "기업 가입이 승인되었습니다."),
    COMPANY_REJECTED("기업 거절", "기업 가입이 거절되었습니다."),

    // 시스템 알림
    SYSTEM_NOTICE("시스템 공지", "시스템 공지사항입니다.");

    private final String title;
    private final String message;
}