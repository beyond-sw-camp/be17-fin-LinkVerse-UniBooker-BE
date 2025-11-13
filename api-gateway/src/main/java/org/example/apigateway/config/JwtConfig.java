package org.example.apigateway.config;

import org.example.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 유틸리티 Bean 설정
 * - Common 모듈의 JwtUtil을 Spring Bean으로 등록
 * - application.yml의 JWT 설정값 주입
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    /**
     * JwtUtil Bean 생성
     */
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secretKey, accessTokenValidity, refreshTokenValidity);
    }
}