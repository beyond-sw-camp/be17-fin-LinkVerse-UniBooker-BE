package org.example.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient 설정
 * - Main-Service 내부 API 호출용
 * - Load Balancer 적용 (Eureka 기반)
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${app.main-service.url:lb://api-app}")
    private String mainServiceUrl;

    /**
     * Main-Service 호출용 WebClient
     * - Eureka를 통한 로드밸런싱 적용
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * Main-Service 전용 WebClient
     */
    @Bean
    public WebClient mainServiceWebClient(WebClient.Builder webClientBuilder) {
        log.info("WebClient 초기화 - Main-Service URL: {}", mainServiceUrl);

        return webClientBuilder
                .baseUrl(mainServiceUrl)
                .build();
    }
}