package org.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // ⭐ WebSocket + 쿠키 사용 시 절대로 "*" 쓰면 안 됨
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "https://unibooker.kro.kr",
                "http://unibooker.kro.kr",
                "http://www.unibooker.kro.kr",
                "https://www.unibooker.kro.kr"
        ));

        // 허용 메서드
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 허용 헤더
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setExposedHeaders(Arrays.asList("*"));

        // 쿠키 허용
        corsConfig.setAllowCredentials(true);

        // preflight 캐시
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // ⭐ WebSocket 경로 포함해서 등록
        source.registerCorsConfiguration("/**", corsConfig);
        source.registerCorsConfiguration("/ws/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
