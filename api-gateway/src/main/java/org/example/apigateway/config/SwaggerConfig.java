package org.example.apigateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API Gateway 통합 Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.unibooker.kro.kr")
                                .description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("JWT Cookie Auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("accessToken")
                                        .description("JWT 토큰 (HTTP-Only Cookie)")))
                .addSecurityItem(new SecurityRequirement().addList("JWT Cookie Auth"));
    }

    private Info createApiInfo() {
        return new Info()
                .title("UniBooker API Gateway")
                .version("1.0.0")
                .description(createDescription())
                .contact(new Contact()
                        .name("LinkVerse Team")
                        .email("support@unibooker.com"));
    }

    private String createDescription() {
        return """
                # Swagger UI 사용 가이드
                
                ## 1. 서비스 선택
                ### 우측 상단 드롭다운에서 원하는 서비스 선택
                - **API Gateway**: 전체 시스템 개요 및 라우팅 정보
                - **API-APP**: 사용자/기업/인증/알림 API
                - **API-RESOURCE**: 리소스 관리 API
                - **API-RESERVATION**: 예약 관리 API
                - **API-QUEUE**: 대기열 관리 API
                - **API-STATISTICS**: 통계/대시보드 API
                
                ## 2. API 테스트 방법
                - API 항목 클릭 → "Try it out" 버튼 클릭
                - 필수 파라미터 입력 (빨간색 별표 항목)
                - "Execute" 버튼으로 실행
                - 하단에서 응답 확인 (Response body, Response headers)
                
                ## 3. 인증 필요 API
                - 로그인 API 먼저 호출 (예: POST /api/admins/login)
                - 자동으로 쿠키에 JWT 토큰 저장됨
                - 이후 인증 필요한 API 자동으로 토큰 포함되어 호출
                - 로그아웃 시 쿠키 자동 삭제
                
                ## 4. 주의사항
                - Swagger UI에서는 브라우저 쿠키 사용
                - Postman 등 외부 도구 사용 시 쿠키 수동 설정 필요
                - 개발 환경에서만 사용 (운영 환경 주의)
                
                # API Gateway
                
                ## 주요 기능
                - **인증/인가**: JWT 토큰 검증 및 권한 확인
                - **라우팅**: 요청을 적절한 마이크로서비스로 전달
                - **로드밸런싱**: Eureka 기반 동적 로드밸런싱
                - **통합 문서**: 모든 서비스의 API 문서 통합 제공
                
                ## 시스템 구성 (MSA)
                - **API Gateway (8080)**: 인증, 라우팅
                - **API-APP (8085)**: 사용자/기업/인증/알림
                - **API-RESOURCE (8086)**: 리소스/그룹/카테고리
                - **API-RESERVATION (8087)**: 예약 생성/조회/취소
                - **API-QUEUE (8088)**: 대기열 관리 (Redis)
                - **API-STATISTICS (8089)**: 통계/대시보드
                
                ## 인증 방식
                **JWT + HTTP-Only Cookie**
                - **SUPER**: `superAccessToken` / `superRefreshToken`
                - **ADMIN**: `adminAccessToken` / `adminRefreshToken`
                - **MANAGER**: `managerAccessToken` / `managerRefreshToken`
                - **USER**: `userAccessToken` / `userRefreshToken`
                
                ## 권한 체계
                - **SUPER**: 플랫폼 전체 관리
                - **ADMIN**: 기업 관리
                - **MANAGER**: 일부 관리 권한
                - **USER**: 일반 사용자
                
                ## 공통 응답
                - **성공**: { "code": 200, "message": "성공", "data": {...} }
                - **에러**: { "code": 40001, "message": "에러 메시지", "data": null }
                
                ## 에러 코드
                - **10000-19999**: 공통 (200, 400, 401, 404, 500)
                - **20000-29999**: 인증/권한
                - **30000-39999**: 사용자
                - **40000-49999**: 기업
                - **50000-59999**: 리소스
                - **60000-69999**: 예약
                - **70000-79999**: 대기열
                - **80000-89999**: 통계
                
                ## 라우팅 규칙
                - /api/users/*, /api/admins/* → `API-APP`
                - /api/resource/* → `API-RESOURCE`
                - /api/reservation/* → `API-RESERVATION`
                - /api/queues/* → `API-QUEUE`
                - /api/dashboard/* → `API-STATISTICS`
                - /ws/** → WebSocket `(API-APP)`
                """;
    }
}