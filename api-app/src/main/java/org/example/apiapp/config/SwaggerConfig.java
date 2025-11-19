package org.example.apiapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API-APP Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Main Service API")
                        .version("1.0.0")
                        .description(createDescription())
                        .contact(new Contact()
                                .name("LinkVerse Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Direct Access"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via Gateway")
                ));
    }

    private String createDescription() {
        return """
                # API-APP (Main Service)
                
                ## 주요 기능
                - **사용자 관리**: SUPER, ADMIN, MANAGER, USER 역할 관리
                - **기업 관리**: 기업 등록, 승인/거절, 상태 관리
                - **인증/인가**: JWT + HTTP-Only Cookie 기반 인증
                - **알림**: 이메일 & WebSocket 실시간 알림 (21가지 유형)
                - **이미지 업로드**: S3 Presigned URL 방식
                
                ## 인증 방식
                **JWT Token → HttpOnly Cookie 저장**
                - **SUPER**: `superAccessToken` / `superRefreshToken`
                - **ADMIN**: `adminAccessToken` / `adminRefreshToken`
                - **MANAGER**: `managerAccessToken` / `managerRefreshToken`
                - **USER**: `userAccessToken` / `userRefreshToken`
                
                ## 회원가입 정책
                - 하나의 계정 = 하나의 권한
                - 동일 이메일로 여러 기업의 USER 계정 가능
                - ADMIN/MANAGER 이메일 중복 불가
                
                ## 기업 상태
                - **PENDING**: 승인 대기
                - **ACTIVE**: 서비스 운영 중
                - **SUSPENDED**: 서비스 정지
                - **REJECTED**: 신청 거절 (삭제)
                
                ## Company Slug
                기업 고유 식별자 (예: `company-a`, `samsung-corp`)
                - 규칙: 소문자, 숫자, 하이픈만 허용 (3-30자)
                - 용도: URL 서브도메인 (`https://company-a.unibooker.com`)
                
                ## 에러 코드
                - **20000-29999**: 인증/권한 에러
                - **30000-39999**: 사용자 에러
                - **40000-49999**: 기업 에러
                """;
    }
}