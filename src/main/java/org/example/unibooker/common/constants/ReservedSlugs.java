package org.example.unibooker.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Company Slug 예약어 관리 클래스
 */
public class ReservedSlugs {

    /**
     * 예약된 slug 목록
     * 시스템 예약어, 엔드포인트와 충돌 가능한 단어 등
     */
    public static final List<String> RESERVED_SLUGS = Arrays.asList(
            // 시스템 예약어
            "admin",
            "api",
            "public",
            "system",
            "root",

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

            // 일반 예약어
            "dashboard",
            "settings",
            "profile",
            "about",
            "help",
            "support",
            "contact",

            // HTTP 메서드
            "get",
            "post",
            "put",
            "delete",
            "patch",

            // 회사/조직 관련
            "company",
            "companies",
            "organization",
            "org",

            // 리소스 관련
            "resource",
            "resources",
            "reservation",
            "reservations",

            // 통계/관리
            "stats",
            "statistics",
            "analytics",
            "report",
            "reports",
            "notification",
            "notifications",

            // 기타
            "home",
            "index",
            "test",
            "demo",
            "example",
            "sample"
    );

    /**
     * slug가 예약어인지 확인
     */
    public static boolean isReserved(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        return RESERVED_SLUGS.contains(slug.toLowerCase());
    }

    // 인스턴스화 방지
    private ReservedSlugs() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}