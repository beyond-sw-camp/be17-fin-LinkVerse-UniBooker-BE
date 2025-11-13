package com.unibooker.main.config;

import com.unibooker.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 설정
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secretKey, accessTokenValidity, refreshTokenValidity);
    }
}