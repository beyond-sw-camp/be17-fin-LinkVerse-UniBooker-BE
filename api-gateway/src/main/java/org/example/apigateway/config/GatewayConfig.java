package org.example.apigateway.config;

import org.example.apigateway.filter.CompanyStatusFilter;
import org.example.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API Gateway 라우팅 설정
 * - 서비스별 라우팅 규칙 정의
 * - JWT 인증 필터 적용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CompanyStatusFilter companyStatusFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ========== Company URL 패턴 처리 (최우선) ==========

                /**
                 * /c/{slug}/** 경로 처리
                 * - Company 상태 확인 후 적절한 처리
                 * - SUSPENDED → 정지 페이지
                 * - ACTIVE/ADMIN_PENDING → Main-Service로 전달
                 */
                .route("company-service", r -> r
                        .path("/c/**")
                        .filters(f -> f
                                .filter(companyStatusFilter.apply(new CompanyStatusFilter.Config()))
                                .rewritePath("/c/(?<slug>.*?)(?<remaining>/.*)?", "/api/c/${slug}${remaining}")
                        )
                        .uri("lb://api-app"))

                // ========== Main Service - Admin API ==========

                // Auth API (인증 불필요)
                .route("auth-refresh", r -> r
                        .path("/api/auth/refresh")
                        .uri("lb://api-app"))

                // Admin 회원가입 (인증 불필요)
                .route("admin-signup", r -> r
                        .path("/api/admins/signup")
                        .uri("lb://api-app"))

                // Admin 상태 조회 (인증 불필요)
                .route("admin-status", r -> r
                        .path("/api/admins/status")
                        .uri("lb://api-app"))

                // Admin 이메일 확인 (인증 불필요)
                .route("admin-check-email", r -> r
                        .path("/api/admins/check-email")
                        .uri("lb://api-app"))

                // Admin 로그인 (인증 불필요)
                .route("admin-login", r -> r
                        .path("/api/admins/login")
                        .uri("lb://api-app"))

                // Admin API (인증 필요) - logout, /me 등
                .route("admin-protected", r -> r
                        .path("/api/admins/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Main Service - Super API ==========

                // Super 로그인 (인증 불필요)
                .route("super-login", r -> r
                        .path("/api/super/login")
                        .uri("lb://api-app"))

                // Super API (인증 필요)
                .route("super-protected", r -> r
                        .path("/api/super/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Main Service - User API ==========

                // User 회원가입 (인증 불필요)
                .route("user-signup", r -> r
                        .path("/api/users/signup")
                        .uri("lb://api-app"))

                // User 로그인 (인증 불필요)
                .route("user-login", r -> r
                        .path("/api/users/login")
                        .uri("lb://api-app"))

                // User 공개 API (인증 불필요)
                .route("user-public", r -> r
                        .path("/api/users/check-email", "/api/users/accounts",
                                "/api/users/reset-password", "/api/users/find-email")
                        .uri("lb://api-app"))

                // User API (인증 필요)
                .route("user-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Main Service - Company API ==========

                // Company 공개 API (인증 불필요)
                .route("company-public", r -> r
                        .path("/api/companies/slug/**",           // 기업 정보 조회
                                "/api/companies/check-slug",        // Slug 중복 확인
                                "/api/companies/check-business-number") // 사업자번호 확인
                        .uri("lb://api-app"))

                // Company API (인증 필요)
                .route("company-protected", r -> r
                        .path("/api/companies/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Main Service - Notification API ==========

                // Notification API (인증 필요)
                .route("notification-api", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Statistics Service - Dashboard API ==========

                // Dashboard API (인증 필요)
                .route("dashboard-api", r -> r
                        .path("/api/dashboard/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-statistics"))

                // ========== Actuator (Health Check) ==========

                // Actuator (인증 불필요)
                .route("actuator", r -> r
                        .path("/actuator/**")
                        .uri("lb://api-app"))

                .build();
    }
}