package org.example.apiapp.domain.notification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 카테고리
 */
@Getter
public enum NotificationType {

    // ========== 예약 관련 (Reservation) ==========

    RESERVATION_CONFIRMED(
            "[%s] 예약 확정",
            "Reservation Confirmed",
            "USER",
            "예약이 정상적으로 확정되었습니다."
    ),

    RESERVATION_CANCELLED(
            "[%s] 예약 취소",
            "Reservation Cancelled",
            "USER",
            "예약이 취소되었습니다."
    ),

    RESERVATION_REMINDER(
            "[%s] 예약 알림",
            "Reservation Reminder",
            "USER",
            "예약하신 %s 일정이 곧 시작됩니다."
    ),

    // ========== 사용자 관련 (User) ==========

    WELCOME(
            "가입 환영",
            "Welcome",
            "USER",
            "%s에 가입하신 것을 환영합니다."
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
            "[%s] 가입 승인 완료",
            "Company Approved",
            "ADMIN",
            "%s 가입 신청이 승인되었습니다. 서비스 이용을 환영합니다!"
    ),

    MANAGER_CREATED(
            "매니저 계정 생성",
            "Manager Created",
            "MANAGER",
            "매니저 계정이 등록되어 관리 기능을 사용할 수 있습니다."
    ),

    // ========== 리소스 관리 (Resource - ADMIN용) ==========

    RESOURCE_FULLY_BOOKED(
            "[%s] 예약 마감 안내",
            "Resource Fully Booked",
            "ADMIN",
            "서비스 %s 예약이 마감되었습니다."
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

    private final String title;
    private final String englishTitle;
    private final String targetRole;
    private final String message;

    NotificationType(String title, String englishTitle,
                     String targetRole, String message) {
        this.title = title;
        this.englishTitle = englishTitle;
        this.targetRole = targetRole;
        this.message = message;
    }

    // 제목, 메시지에 %s 채워주는 함수
    public String formatTitle(Object... args) {
        return String.format(title, args);
    }

    public String formatMessage(Object... args) {
        return String.format(message, args);
    }

    // 사용예시
    // NotificationType.RESERVATION_REMINDER.formatTitle("회의실 A");

    /**
     * 언어에 따른 카테고리명 반환
     */
    public String getLocalizedName(String language) {
        if ("ko".equalsIgnoreCase(language) || "kr".equalsIgnoreCase(language)) {
            return title;
        }
        return englishTitle;
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