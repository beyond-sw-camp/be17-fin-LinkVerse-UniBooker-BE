package org.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.common.model.CompanyStatus;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Company 상태 확인 필터
 * - /c/{slug}/** 경로에서 Company 상태 확인
 * - SUSPENDED → 정지 페이지 리다이렉트
 * - PENDING/REJECTED → 404
 * - ACTIVE/ADMIN_PENDING → 통과
 */
@Slf4j
@Component
public class CompanyStatusFilter extends AbstractGatewayFilterFactory<CompanyStatusFilter.Config> {

    private final WebClient mainServiceWebClient;

    /** /c/{slug}/** 패턴 */
    private static final Pattern COMPANY_URL_PATTERN = Pattern.compile("^/c/([a-z0-9-]+)(/.*)?$");

    /** 정지 페이지 URL */
    private static final String SUSPENDED_PAGE_URL = "/suspended";

    public CompanyStatusFilter(WebClient mainServiceWebClient) {
        super(Config.class);
        this.mainServiceWebClient = mainServiceWebClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            log.debug("CompanyStatusFilter 처리 - 경로: {}", path);

            // 1. /c/{slug}/** 패턴 확인
            Matcher matcher = COMPANY_URL_PATTERN.matcher(path);
            if (!matcher.matches()) {
                log.debug("Company URL 패턴 아님 - 필터 통과");
                return chain.filter(exchange);
            }

            // 2. Company Slug 추출
            String companySlug = matcher.group(1);
            log.info("Company Slug 추출: {}", companySlug);

            // 3. Main-Service에서 Company 상태 조회
            return getCompanyStatus(companySlug)
                    .flatMap(status -> {
                        log.info("Company 상태 확인 - slug: {}, status: {}", companySlug, status);

                        // 4. 상태별 처리
                        switch (status) {
                            case ACTIVE:
                                // 정상 서비스 → 요청 통과
                                log.info("Company 정상 상태 - 요청 통과");
                                return chain.filter(exchange);

                            case SUSPENDED:
                                // 서비스 정지 → 정지 페이지로 리다이렉트
                                log.warn("Company 정지 상태 - 정지 페이지로 리다이렉트");
                                return redirectToSuspendedPage(exchange, companySlug);

                            case PENDING:
                            case REJECTED:
                            default:
                                // 미승인/거절 → 404
                                log.warn("Company 접근 불가 상태: {} - 404 반환", status);
                                return onError(exchange, "서비스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
                        }
                    })
                    .onErrorResume(e -> {
                        // Company 조회 실패 → 404
                        log.error("Company 상태 조회 실패 - slug: {}, error: {}", companySlug, e.getMessage());
                        return onError(exchange, "서비스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
                    });
        };
    }

    /**
     * Main-Service에서 Company 상태 조회
     */
    private Mono<CompanyStatus> getCompanyStatus(String companySlug) {
        String url = String.format("/api/companies/internal/slug/%s/status", companySlug);

        log.debug("Main-Service 호출: {}", url);

        return mainServiceWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(CompanyStatusResponse.class)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return response.getData().getStatus();
                    }
                    throw new RuntimeException("Company 상태 조회 실패");
                })
                .doOnError(e -> log.error("Company 상태 조회 API 호출 실패: {}", e.getMessage()));
    }

    /**
     * 정지 페이지로 리다이렉트
     */
    private Mono<Void> redirectToSuspendedPage(ServerWebExchange exchange, String companySlug) {
        exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        exchange.getResponse().getHeaders().setLocation(
                java.net.URI.create(SUSPENDED_PAGE_URL + "?company=" + companySlug)
        );
        return exchange.getResponse().setComplete();
    }

    /**
     * 에러 응답 반환
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json; charset=UTF-8");

        String errorResponse = String.format(
                "{\"success\":false,\"code\":%d,\"message\":\"%s\"}",
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

    // ========== 내부 DTO 클래스 ==========

    /**
     * Main-Service API 응답 구조
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class CompanyStatusResponse {
        private boolean success;
        private int code;
        private String message;
        private CompanyStatusData data;

        public boolean isSuccess() {
            return success;
        }
    }

    /**
     * Company 상태 데이터
     */
    @lombok.Getter
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class CompanyStatusData {
        private Long companyId;
        private String companySlug;
        private CompanyStatus status;
    }
}