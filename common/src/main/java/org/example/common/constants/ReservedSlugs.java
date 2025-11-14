package org.example.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Company Slug 예약어 관리 클래스
 * - 시스템 예약어 정의
 * - Slug 예약어 검증
 */
public class ReservedSlugs {

    /**
     * 예약된 Slug 목록
     * - 시스템 예약어
     * - 엔드포인트와 충돌 가능한 단어
     * - HTTP 메서드명
     */
    public static final List<String> RESERVED_SLUGS = Arrays.asList(
            // 시스템 예약어
            "admin",
            "api",
            "public",
            "system",
            "root",
            "super",

            // 사용자 관련
            "user",
            "users",
            "customer",
            "customers",

            // 인증 관련
            "login",
            "logout",
            "signup",
            "signin",
            "register",
            "auth",
            "token",
            "refresh",

            // 일반 예약어
            "dashboard",
            "settings",
            "profile",
            "about",
            "help",
            "support",
            "contact",
            "terms",
            "privacy",
            "policy",

            // HTTP 메서드
            "get",
            "post",
            "put",
            "delete",
            "patch",
            "head",
            "options",

            // 회사/조직 관련
            "company",
            "companies",
            "organization",
            "org",
            "enterprise",

            // 리소스 관련
            "resource",
            "resources",
            "reservation",
            "reservations",
            "booking",
            "bookings",

            // 통계/관리
            "stats",
            "statistics",
            "analytics",
            "report",
            "reports",
            "notification",
            "notifications",
            "alert",
            "alerts",

            // 기타
            "home",
            "index",
            "test",
            "demo",
            "example",
            "sample",
            "health",
            "actuator",
            "swagger",
            "docs"
    );

    /**
     * Slug가 예약어인지 확인
     *
     * @param slug 확인할 Slug (대소문자 구분 없음)
     * @return 예약어면 true, 아니면 false
     */
    public static boolean isReserved(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        return RESERVED_SLUGS.contains(slug.toLowerCase());
    }

    /**
     * 인스턴스화 방지
     */
    private ReservedSlugs() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}