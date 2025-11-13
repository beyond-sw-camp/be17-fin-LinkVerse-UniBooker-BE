package com.unibooker.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * PasswordEncoder Bean 등록
     * - BCrypt 암호화 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security Filter Chain 설정
     * - 모든 요청 허용 (JWT 검증은 API Gateway에서)
     * - CSRF 비활성화
     * - Session 사용 안함 (Stateless)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.disable())

                // Session 사용 안함 (Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 모든 요청 허용 (인증은 API Gateway에서)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}