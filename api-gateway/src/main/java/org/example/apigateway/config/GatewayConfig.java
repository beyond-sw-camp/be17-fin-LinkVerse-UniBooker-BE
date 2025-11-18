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

                // ========== Resource Service (추가!) ==========
                .route("resource-service", r -> r
                        .path("/api/resource/**", "/api/resource-group/**",
                                "/api/custom-field/**", "/api/category-field/**", "/api/timeslot/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-resource"))

                // ========== Reservation Service (추가!) ==========
                .route("reservation-service", r -> r
                        .path("/api/reservation/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-reservation"))

                // ========== Queue Service (추가!) ==========
                .route("queue-service", r -> r
                        .path("/api/queues/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-queue"))

                // ========== Statistics Service ==========
                .route("statistics-service", r -> r
                        .path("/api/dashboard/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-statistics"))

                // ========== Company URL 패턴 처리 ==========
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
                        .path("/api/admins/login", "/api/admins/signup",
                                "/api/admins/status", "/api/admins/check-email")
                        .uri("lb://api-app"))

                // Admin API (인증 필요) - logout, /me 등
                .route("admin-protected", r -> r
                        .path("/api/admins/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Super API ==========
                .route("super-login", r -> r
                        .path("/api/super/login")
                        .uri("lb://api-app"))

                // Super API (인증 필요)
                .route("super-protected", r -> r
                        .path("/api/super/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== User API ==========
                .route("user-public", r -> r
                        .path("/api/users/signup", "/api/users/login",
                                "/api/users/check-email", "/api/users/accounts",
                                "/api/users/reset-password", "/api/users/find-email")
                        .uri("lb://api-app"))

                // User API (인증 필요)
                .route("user-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Company API ==========
                .route("company-public", r -> r
                        .path("/api/companies/slug/**", "/api/companies/check-slug",
                                "/api/companies/check-business-number")
                        .uri("lb://api-app"))

                // Company API (인증 필요)
                .route("company-protected", r -> r
                        .path("/api/companies/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Notification API ==========
                .route("notification-api", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Auth API ==========
                .route("auth-refresh", r -> r
                        .path("/api/auth/refresh")
                        .uri("lb://api-app"))

                // ========== WebSocket Route ==========
                .route("websocket-route", r -> r
                        .path("/ws/**")
                        .filters(f -> f
                                .removeRequestHeader("Origin") // WebSocket handshake용
                        )
                        .uri("lb://api-app")
                )

                // ========== Actuator (Health Check) ==========

                // Actuator (인증 불필요)
                .route("actuator", r -> r
                        .path("/actuator/**")
                        .uri("lb://api-app"))

                .build();
    }
}