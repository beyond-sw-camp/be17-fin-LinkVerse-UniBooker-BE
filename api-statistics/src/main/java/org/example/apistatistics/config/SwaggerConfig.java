package org.example.apistatistics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API-STATISTICS Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Statistics Service API")
                        .version("1.0.0")
                        .description(createDescription())
                        .contact(new Contact()
                                .name("LinkVerse Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8089")
                                .description("Direct Access"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via Gateway")
                ));
    }

    private String createDescription() {
        return """
                # API-STATISTICS (Statistics Service)
                
                ## 주요 기능
                - **대시보드**: 관리자용 실시간 대시보드 데이터
                - **예약 통계**: 기간별 예약 현황 분석
                - **리소스 분석**: 인기 리소스, 이용률 통계
                - **사용자 통계**: 가입자 수, 활성 사용자 추이
                - **실시간 모니터링**: 현재 예약 현황, 대기열 현황
                
                ## 대시보드 지표
                
                **관리자 대시보드**:
                - 오늘 예약 수 / 취소율
                - 이번 달 예약 수 / 전월 대비
                - 리소스 이용률 Top 5
                - 최근 7일 예약 추이 그래프
                - 대기열 현황 (대기 중, 승격 대기)
                
                **플랫폼 관리자 대시보드**:
                - 전체 기업 수 / 활성 기업
                - 전체 사용자 수 / 월별 증가율
                - 기업별 예약 수 Top 10
                - 시스템 부하 현황
                
                ## 통계 기간
                - **일별**: 최근 7일, 30일
                - **주별**: 최근 4주, 12주
                - **월별**: 최근 12개월
                - **연도별**: 최근 3년
                
                ## 분석 지표
                
                **예약 지표**:
                - 총 예약 수
                - 예약 확정률 (확정 / 전체)
                - 예약 취소율 (취소 / 전체)
                - 노쇼율 (노쇼 / 확정)
                - 평균 예약 시간
                
                **리소스 지표**:
                - 리소스 이용률 (예약 시간 / 운영 시간)
                - 시간대별 예약 분포
                - 인기 리소스 순위
                - 미사용 리소스 목록
                
                **사용자 지표**:
                - DAU (Daily Active Users)
                - MAU (Monthly Active Users)
                - 신규 가입자 수
                - 이용 빈도별 분류
                
                ## 데이터 집계
                - **실시간**: Redis Cache (5분 갱신)
                - **일별**: Batch Job (매일 자정)
                - **월별**: Batch Job (매월 1일)
                
                ## 성능 최적화
                - **Materialized View**: 자주 조회되는 통계 미리 계산
                - **Redis Caching**: 대시보드 데이터 캐싱 (TTL: 5분)
                - **Read Replica**: 통계 조회는 읽기 전용 DB 사용
                """;
    }
}