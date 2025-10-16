package org.example.unibooker.domain.notification.model;

import lombok.Getter;

/**
 * 알림 카테고리 정의
 * - 프로젝트 도메인별로 분류
 * - targetRole로 권한별 알림 구분
 */
@Getter
public enum NotificationCategory {

    // ========== 예약 관련 (Reservation) ==========

    RESERVATION_CONFIRMED(
            "예약 확정",
            "Reservation Confirmed",
            "USER",
            "예약이 정상적으로 확정되었습니다."
    ),

    RESERVATION_CANCELLED(
            "예약 취소",
            "Reservation Cancelled",
            "USER",
            "예약이 취소되었습니다."
    ),

    RESERVATION_REMINDER(
            "예약 알림",
            "Reservation Reminder",
            "USER",
            "예약 시간이 곧 도래합니다."
    ),

    // ========== 대기열 관련 (Waitlist) ==========

    WAITLIST_REGISTERED(
            "대기열 등록",
            "Waitlist Registered",
            "USER",
            "대기열에 등록되었습니다."
    ),

    WAITLIST_PROMOTED(
            "대기열 승격",
            "Waitlist Promoted",
            "USER",
            "대기열에서 승격되어 예약이 가능합니다."
    ),

    WAITLIST_EXPIRED(
            "대기열 만료",
            "Waitlist Expired",
            "USER",
            "대기 시간이 만료되었습니다."
    ),

    // ========== 사용자 관련 (User) ==========

    WELCOME(
            "가입 환영",
            "Welcome",
            "USER",
            "UniBooker에 가입하신 것을 환영합니다."
    ),

    PASSWORD_CHANGED(
            "비밀번호 변경",
            "Password Changed",
            "ALL",
            "비밀번호가 성공적으로 변경되었습니다."
    ),

    ACCOUNT_ACTIVATED(
            "계정 활성화",
            "Account Activated",
            "ALL",
            "계정이 활성화되었습니다."
    ),

    ACCOUNT_SUSPENDED(
            "계정 정지",
            "Account Suspended",
            "ALL",
            "계정이 정지되었습니다."
    ),

    // ========== 기업 관리 (Company - ADMIN용) ==========

    COMPANY_APPROVED(
            "기업 승인",
            "Company Approved",
            "ADMIN",
            "기업 가입이 승인되었습니다."
    ),

    COMPANY_REJECTED(
            "기업 거절",
            "Company Rejected",
            "ADMIN",
            "기업 가입이 거절되었습니다."
    ),

    MANAGER_CREATED(
            "매니저 생성",
            "Manager Created",
            "ADMIN",
            "새로운 매니저 계정이 생성되었습니다."
    ),

    // ========== 리소스 관리 (Resource - ADMIN용) ==========

    RESOURCE_LOW_STOCK(
            "리소스 부족",
            "Resource Low Stock",
            "ADMIN",
            "리소스 잔여량이 부족합니다."
    ),

    RESOURCE_FULLY_BOOKED(
            "리소스 만석",
            "Resource Fully Booked",
            "ADMIN",
            "리소스가 완전히 예약되었습니다."
    ),

    DAILY_REPORT(
            "일일 리포트",
            "Daily Report",
            "ADMIN",
            "일일 예약 현황 리포트입니다."
    ),

    // ========== 플랫폼 관리 (SUPER용) ==========

    NEW_COMPANY_REQUEST(
            "신규 기업 신청",
            "New Company Request",
            "SUPER",
            "새로운 기업 가입 신청이 접수되었습니다."
    ),

    SYSTEM_ALERT(
            "시스템 경고",
            "System Alert",
            "SUPER",
            "시스템에 이상이 감지되었습니다."
    ),

    MONTHLY_STATISTICS(
            "월간 통계",
            "Monthly Statistics",
            "SUPER",
            "월간 플랫폼 이용 통계입니다."
    ),

    // ========== 공통 알림 ==========

    SYSTEM_NOTICE(
            "시스템 공지",
            "System Notice",
            "ALL",
            "시스템 공지사항입니다."
    ),

    SYSTEM_MAINTENANCE(
            "시스템 점검",
            "System Maintenance",
            "ALL",
            "시스템 점검이 예정되어 있습니다."
    );

    private final String koreanName;
    private final String englishName;
    private final String targetRole;
    private final String defaultMessage;

    NotificationCategory(String koreanName, String englishName,
                         String targetRole, String defaultMessage) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.targetRole = targetRole;
        this.defaultMessage = defaultMessage;
    }

    /**
     * 언어에 따른 카테고리명 반환
     */
    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return koreanName;
        }
        return englishName;
    }

    /**
     * 특정 권한이 받을 수 있는 카테고리인지 확인
     */
    public boolean isForRole(String role) {
        return "ALL".equals(this.targetRole) || this.targetRole.equals(role);
    }

    /**
     * USER용 카테고리인지 확인
     */
    public boolean isForUser() {
        return "USER".equals(this.targetRole) || "ALL".equals(this.targetRole);
    }

    /**
     * ADMIN용 카테고리인지 확인
     */
    public boolean isForAdmin() {
        return "ADMIN".equals(this.targetRole) || "ALL".equals(this.targetRole);
    }

    /**
     * SUPER용 카테고리인지 확인
     */
    public boolean isForSuper() {
        return "SUPER".equals(this.targetRole) || "ALL".equals(this.targetRole);
    }
}