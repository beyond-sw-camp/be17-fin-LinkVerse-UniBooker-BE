package org.example.apigateway.filter;

import org.example.common.util.CookieUtil;
import org.example.common.model.UserRole;
import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;
import org.example.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * JWT 인증 필터
 * - Authorization 헤더에서 JWT 토큰 검증
 * - 검증 성공 시 사용자 정보를 헤더에 추가 (X-User-Id, X-User-Email, X-User-Role, X-Company-Id)
 * - 검증 실패 시 401 Unauthorized 반환
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    /**
     * 인증 제외 경로 목록
     * - 로그인, 회원가입, 공개 API
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            // ===== Admin 관련 =====
            "/api/admins/signup",
            "/api/admins/signup/status",  // ✅ 추가
            "/api/admins/login",

            // ===== User 관련 =====
            "/api/users/signup",
            "/api/users/login",
            "/api/users/check-email",
            "/api/users/accounts",
            "/api/users/reset-password",
            "/api/users/find-email",

            // ===== Company 공개 API (일반 사용자용) =====
            "/api/companies/slug",                    // ✅ 추가
            "/api/companies/check-slug",              // ✅ 추가
            "/api/companies/check-business-number",   // ✅ 추가

            // ===== Super Admin =====
            "/api/super/login",

            // ===== Auth =====
            "/api/auth/refresh",   // ✅ 추가 (토큰 갱신)

            // ===== Actuator =====
            "/actuator"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("JWT 필터 처리 - 경로: {}", path);

            // 인증 제외 경로 확인
            if (isExcludedPath(path)) {
                log.info("JWT 검증 제외 경로: {}", path);
                return chain.filter(exchange);
            }

            // Authorization 헤더에서 토큰 추출
            String token = extractToken(request);

            if (token == null) {
                log.warn("JWT 토큰이 없습니다. 경로: {}", path);
                return onError(exchange, "JWT 토큰이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            try {
                // 토큰 검증
                if (!jwtUtil.validateToken(token)) {
                    log.warn("JWT 토큰이 유효하지 않습니다. 경로: {}", path);
                    return onError(exchange, "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED);
                }

                // 토큰에서 사용자 정보 추출
                Long userId = jwtUtil.getUserId(token);
                String email = jwtUtil.getEmail(token);
                String role = jwtUtil.getRole(token);
                Long companyId = jwtUtil.getCompanyId(token);
                String userName = jwtUtil.getUserName(token);

                log.info("JWT 검증 성공 - userId: {}, email: {}, role: {}, companyId: {}, userName: {}",
                        userId, email, role, companyId, userName);

                // 헤더에 사용자 정보 추가 (다운스트림 서비스에서 사용)
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .header("X-Company-Id", companyId != null ? String.valueOf(companyId) : "")
                        .header("X-User-Name", userName != null ? userName : "")
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);

            } catch (Exception e) {
                log.error("JWT 검증 중 오류 발생: {}", e.getMessage(), e);
                return onError(exchange, "토큰 검증 실패", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * JWT 검증 제외 경로 확인
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.contains(path);
    }

    /**
     * JWT 토큰 추출 (우선순위: Authorization 헤더 → Cookie)
     */
    private String extractToken(ServerHttpRequest request) {
        // 1. Authorization 헤더에서 토큰 추출 시도
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            log.debug("Authorization 헤더에서 토큰 추출");
            return bearerToken.substring(7);
        }

        // 2. Cookie에서 토큰 추출 시도
        String tokenFromCookie = extractTokenFromCookie(request);
        if (tokenFromCookie != null) {
            log.debug("Cookie에서 토큰 추출");
            return tokenFromCookie;
        }

        return null;
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     * - 경로 기반 권한 추론하여 해당 쿠키 탐색
     * - 못 찾으면 모든 권한 쿠키 순차 확인
     */
    private String extractTokenFromCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }

        String path = request.getURI().getPath();
        UserRole inferredRole = inferRoleFromPath(path);

        // 특정 권한 경로인 경우 해당 쿠키만 확인
        if (inferredRole != null) {
            String cookieName = CookieUtil.getAccessTokenCookieName(inferredRole);
            HttpCookie cookie = cookies.getFirst(cookieName);
            if (cookie != null) {
                log.debug("경로 기반 토큰 추출 - role: {}, cookieName: {}", inferredRole, cookieName);
                return cookie.getValue();
            }
            return null;
        }

        // API 경로 등: 모든 권한 쿠키 순차 확인
        for (UserRole role : UserRole.values()) {
            String cookieName = CookieUtil.getAccessTokenCookieName(role);
            HttpCookie cookie = cookies.getFirst(cookieName);
            if (cookie != null) {
                log.debug("API 경로 토큰 추출 - role: {}, cookieName: {}", role, cookieName);
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 요청 경로를 기반으로 권한 추론
     */
    private UserRole inferRoleFromPath(String path) {
        if (path.startsWith("/api/users")) {
            return UserRole.USER;
        } else if (path.startsWith("/api/admins") || path.startsWith("/api/managers")) {
            return UserRole.ADMIN;  // ADMIN과 MANAGER는 같은 쿠키 사용
        } else if (path.startsWith("/api/super")) {
            return UserRole.SUPER;
        }
        return null;
    }

    /**
     * 에러 응답 반환
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json; charset=UTF-8");

        String errorResponse = String.format(
                "{\"success\":false,\"code\":\"%s\",\"message\":\"%s\"}",
                httpStatus.value(),
                message
        );

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }

    /**
     * 필터 설정 클래스
     */
    public static class Config {
        // Configuration properties (필요시 추가)
    }
}