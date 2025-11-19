package org.example.apiresource.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API-RESOURCE Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Resource Service API")
                        .version("1.0.0")
                        .description(createDescription())
                        .contact(new Contact()
                                .name("LinkVerse Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8086")
                                .description("Direct Access"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via Gateway")
                ));
    }

    private String createDescription() {
        return """
                # API-RESOURCE (Resource Service)
                
                ## 주요 기능
                - **리소스 관리**: 예약 가능한 자원(회의실, 강의실, 장비 등) CRUD
                - **리소스 그룹**: 리소스를 그룹으로 묶어 관리
                - **카테고리 관리**: 리소스 분류 체계 (예: 회의실, 강의실, 장비)
                - **커스텀 필드**: 기업별 맞춤 입력 항목 정의
                - **타임슬롯**: 예약 가능 시간대 세부 설정
                
                ## 리소스 상태
                - **AVAILABLE**: 예약 가능
                - **UNAVAILABLE**: 예약 불가 (점검, 수리 등)
                - **DELETED**: 삭제됨 (Soft Delete)
                
                ## 타임슬롯 설정
                - **시작/종료 시간 지정**
                - **요일별 설정 가능**
                - **시간 단위 설정 (30분, 1시간 등)**
                - **최대 예약 가능 인원 설정**
                
                ## 커스텀 필드 타입
                - **TEXT**: 텍스트 입력
                - **NUMBER**: 숫자 입력
                - **SELECT**: 드롭다운 선택
                - **CHECKBOX**: 체크박스
                - **DATE**: 날짜 선택
                
                ## 권한
                - **ADMIN/MANAGER**: 리소스 생성/수정/삭제
                - **USER**: 리소스 조회만 가능
                """;
    }
}