package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS 설정
 * - 모든 Origin 허용 (개발 환경)
 * - 프로덕션 환경에서는 특정 도메인만 허용해야 함
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 허용할 Origin (개발: 모두 허용, 프로덕션: 특정 도메인만)
        corsConfig.addAllowedOriginPattern("*");

        // 허용할 HTTP 메서드
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용할 헤더
        corsConfig.addAllowedHeader("*");

        // 자격증명(쿠키) 허용
        corsConfig.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (1시간)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
