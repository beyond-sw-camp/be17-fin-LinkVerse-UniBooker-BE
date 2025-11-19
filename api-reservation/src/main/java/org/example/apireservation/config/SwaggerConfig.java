package org.example.apireservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API-RESERVATION Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Reservation Service API")
                        .version("1.0.0")
                        .description(createDescription())
                        .contact(new Contact()
                                .name("LinkVerse Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8087")
                                .description("Direct Access"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via Gateway")
                ));
    }

    private String createDescription() {
        return """
                # API-RESERVATION (Reservation Service)
                
                ## 주요 기능
                - **예약 생성**: 리소스 예약 신청
                - **예약 조회**: 내 예약 목록, 예약 상세
                - **예약 취소**: 예약 취소 및 환불 처리
                - **대기열 관리**: 예약 마감 시 대기열 등록
                - **자동 승격**: 취소 발생 시 대기자 자동 승격
                
                ## 예약 상태
                - **PENDING**: 승인 대기 (관리자 승인 필요 시)
                - **CONFIRMED**: 예약 확정
                - **CANCELLED**: 예약 취소
                - **COMPLETED**: 예약 완료 (종료 시간 경과)
                - **NO_SHOW**: 노쇼 (시작 시간 경과, 사용 안 함)
                
                ## 대기열 시스템
                - 예약 마감 시 대기열 등록
                - 선착순으로 대기 번호 부여
                - 취소 발생 시 1번 대기자 자동 승격
                - 승격 실패 시 다음 순번 대기자에게 기회
                
                ## 대기 상태
                - **WAITING**: 대기 중
                - **PROMOTED**: 승격 완료 (예약 확정)
                - **CANCELLED**: 대기 취소
                - **EXPIRED**: 대기 만료
                
                ## 예약 정책
                - 중복 예약 방지 (동일 시간대)
                - 최소/최대 예약 시간 제한
                - 예약 가능 기간 설정
                - 취소 가능 기간 설정
                
                ## 알림
                - 예약 확정 시 이메일 발송
                - 예약 시작 전 알림 (1시간/30분 전)
                - 대기 순번 도래 알림
                - 자동 승격 알림
                """;
    }
}