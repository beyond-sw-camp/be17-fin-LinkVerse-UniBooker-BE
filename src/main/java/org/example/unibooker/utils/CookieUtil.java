package org.example.unibooker.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.unibooker.domain.user.model.UserRole;

/**
 * JWT 토큰 Cookie 유틸리티
 * - Access Token 및 Refresh Token Cookie 생성/삭제
 * - HttpOnly, Secure, Path, MaxAge 등 보안 설정 통합 관리
 */
public class CookieUtil {

    // ===== 상수 정의 =====

    /** Access Token 만료 시간 (초): 1일 */
    private static final int ACCESS_TOKEN_MAX_AGE = 24 * 60 * 60;

    /** Refresh Token 만료 시간 (초): 7일 */
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

    /** HTTPS 전용 여부 (개발: false, 운영: true) */
    private static final boolean SECURE = true;

    private static final String DOMAIN = "unibooker.r-e.kr";

    // ===== 권한별 쿠키 이름 및 경로 생성 =====

    /**
     * 권한별 Access Token 쿠키 이름 생성
     * - ADMIN → adminAccessToken
     * - MANAGER → adminAccessToken (ADMIN과 동일)
     * - USER → userAccessToken
     * - SUPER → superAccessToken
     */
    private static String createAccessTokenCookieName(UserRole role) {
        // Manager는 Admin 토큰 사용
        if (role == UserRole.MANAGER) {
            return "adminAccessToken";
        }
        return role.name().toLowerCase() + "AccessToken";
    }

    /**
     * 권한별 Refresh Token 쿠키 이름 생성
     * - ADMIN → adminRefreshToken
     * - MANAGER → adminRefreshToken (ADMIN과 동일)
     * - USER → userRefreshToken
     * - SUPER → superRefreshToken
     */
    private static String createRefreshTokenCookieName(UserRole role) {
        // Manager는 Admin 토큰 사용
        if (role == UserRole.MANAGER) {
            return "adminRefreshToken";
        }
        return role.name().toLowerCase() + "RefreshToken";
    }

    /**
     * 권한별 쿠키 경로 설정
     * - 모든 역할을 "/" 로 통일하여 API 경로에서도 쿠키 전송 보장
     * - 쿠키 이름(adminAccessToken, superAccessToken 등)으로 권한 구분
     */
    private static String getCookiePath(UserRole role) {
        return "/";  // 모든 권한 통일
    }

    // ===== 다중 역할 로그인 방지 =====

    /**
     * 모든 역할의 토큰 Cookie 삭제 (단일 세션 정책)
     * - 새로운 역할로 로그인 시 기존 모든 역할 쿠키 삭제
     * - 브라우저 내 단일 세션만 유지
     */
    public static void deleteAllRolesCookies(HttpServletResponse response) {
        for (UserRole role : UserRole.values()) {
            response.addCookie(deleteAccessTokenCookie(role));
            response.addCookie(deleteRefreshTokenCookie(role));
        }
    }

    // ===== Cookie 생성 =====

    /**
     * 권한별 Access Token Cookie 생성
     * - 권한에 따라 쿠키 이름과 경로가 자동 설정됨
     * - HttpOnly: true (XSS 방어)
     * - Secure: false (개발 환경)
     * - MaxAge: 15분
     */
    public static Cookie createAccessTokenCookie(String token, UserRole role) {
        String cookieName = createAccessTokenCookieName(role);
        String path = getCookiePath(role);

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE);
        cookie.setPath(path);
        cookie.setDomain(DOMAIN);
        cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE);
        return cookie;
    }

    /**
     * 권한별 Refresh Token Cookie 생성
     * - 권한에 따라 쿠키 이름과 경로가 자동 설정됨
     * - HttpOnly: true (XSS 방어)
     * - Secure: false (개발 환경)
     * - MaxAge: 7일
     */
    public static Cookie createRefreshTokenCookie(String token, UserRole role) {
        String cookieName = createRefreshTokenCookieName(role);
        String path = getCookiePath(role);

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE);
        cookie.setPath(path);
        cookie.setDomain(DOMAIN);
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        return cookie;
    }

    // ===== Cookie 삭제 =====

    /**
     * 권한별 Access Token Cookie 삭제
     * - value를 null로 설정하고 MaxAge를 0으로 설정하여 즉시 만료
     */
    public static Cookie deleteAccessTokenCookie(UserRole role) {
        String cookieName = createAccessTokenCookieName(role);
        String path = getCookiePath(role);

        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE);
        cookie.setPath(path);
        cookie.setDomain(DOMAIN);
        cookie.setMaxAge(0);
        return cookie;
    }

    /**
     * 권한별 Refresh Token Cookie 삭제
     * - value를 null로 설정하고 MaxAge를 0으로 설정하여 즉시 만료
     */
    public static Cookie deleteRefreshTokenCookie(UserRole role) {
        String cookieName = createRefreshTokenCookieName(role);
        String path = getCookiePath(role);

        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(SECURE);
        cookie.setPath(path);
        cookie.setDomain(DOMAIN);
        cookie.setMaxAge(0);
        return cookie;
    }

    /**
     * 특정 권한의 모든 토큰 Cookie 삭제 (로그아웃 시 사용)
     * - Access Token과 Refresh Token을 모두 삭제
     */
    public static void deleteAllTokenCookies(HttpServletResponse response, UserRole role) {
        response.addCookie(deleteAccessTokenCookie(role));
        response.addCookie(deleteRefreshTokenCookie(role));
    }

    // ===== Cookie 이름 상수 제공 (필터 등에서 사용) =====

    /**
     * 권한별 Access Token 쿠키 이름 반환 (필터에서 사용)
     */
    public static String getAccessTokenCookieName(UserRole role) {
        return createAccessTokenCookieName(role);
    }

    /**
     * 권한별 Refresh Token 쿠키 이름 반환
     */
    public static String getRefreshTokenCookieName(UserRole role) {
        return createRefreshTokenCookieName(role);
    }
}