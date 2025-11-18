package org.example.apigateway.config;

import org.example.apigateway.filter.CompanyStatusFilter;
import org.example.apigateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CompanyStatusFilter companyStatusFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ========== WebSocket (최우선, 필터 없음) ==========
                .route("websocket", r -> r
                        .path("/ws/**", "/ws")
                        .uri("lb://api-app"))

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

                // ========== Admin API ==========
                .route("admin-login", r -> r
                        .path("/api/admins/login", "/api/admins/signup",
                                "/api/admins/status", "/api/admins/check-email")
                        .uri("lb://api-app"))

                .route("admin-protected", r -> r
                        .path("/api/admins/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Super API ==========
                .route("super-login", r -> r
                        .path("/api/super/login")
                        .uri("lb://api-app"))

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

                .route("user-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://api-app"))

                // ========== Company API ==========
                .route("company-public", r -> r
                        .path("/api/companies/slug/**", "/api/companies/check-slug",
                                "/api/companies/check-business-number")
                        .uri("lb://api-app"))

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

                // ========== Actuator ==========
                .route("actuator", r -> r
                        .path("/actuator/**")
                        .uri("lb://api-app"))

                .build();
    }
}