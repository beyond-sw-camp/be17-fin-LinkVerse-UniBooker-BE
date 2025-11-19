package org.example.apiqueue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API-QUEUE Swagger 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Queue Service API")
                        .version("1.0.0")
                        .description(createDescription())
                        .contact(new Contact()
                                .name("LinkVerse Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8088")
                                .description("Direct Access"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Via Gateway")
                ));
    }

    private String createDescription() {
        return """
                # API-QUEUE (Queue Service)
                
                ## 주요 기능
                - **대기열 생성**: Redis 기반 실시간 대기열
                - **순번 조회**: 현재 대기 순번 확인
                - **대기열 이탈**: 대기 취소 처리
                - **이벤트 처리**: Kafka를 통한 비동기 이벤트 처리
                - **동시성 제어**: Redis Lock을 통한 정확한 순번 관리
                
                ## Redis 기반 대기열
                - **Score**: Timestamp (밀리초)
                - **Member**: userId
                - **빠른 조회 속도**
                - **실시간 순번 확인**
                - **자동 정렬 (선착순)**
                - **TTL 설정으로 자동 만료**
                
                ## Kafka 이벤트 유형
                - **RESERVATION_CANCELLED**: 예약 취소, 대기자 승격
                - **QUEUE_JOINED**: 대기열 등록
                - **QUEUE_LEFT**: 대기열 이탈
                - **QUEUE_PROMOTED**: 대기자 승격
                
                ## 동시성 제어
                - **Redis Distributed Lock 사용**
                - **동시 요청 시 순번 충돌 방지**
                - **Lock Timeout**: 5초
                - **Retry**: 3회
                
                ## 대기 정책
                - **최대 대기 인원**: 리소스별 설정
                - **대기 유효 시간**: 24시간
                - **승격 유효 시간**: 10분 (미확인 시 다음 순번)
                
                ## 성능 최적화
                - **Redis Pipelining**: 일괄 조회
                - **Batch Processing**: 승격 처리 배치화
                - **Cache Warming**: 인기 리소스 사전 로딩
                """;
    }
}