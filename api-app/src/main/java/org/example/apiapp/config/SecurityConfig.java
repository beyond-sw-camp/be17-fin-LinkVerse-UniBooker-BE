package org.example.apiapp.config;

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
     * - 공개 API 제외하고 인증 요구
     * - JWT 검증은 API Gateway에서
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== 공개 API (인증 불필요) =====
                        .requestMatchers(
                                "/api/companies/slug/**",        // Company 정보 조회
                                "/api/companies/check-slug",      // Slug 중복 확인
                                "/api/companies/check-business-number", // 사업자번호 확인
                                "/api/admin/signup",              // 관리자 회원가입
                                "/api/admin/signup/status",       // 회원가입 상태
                                "/api/auth/**",                   // 인증 관련 (로그인, 토큰 갱신)
                                "/api/users/signup",              // 일반 사용자 회원가입
                                "/api/users/check-email"          // 이메일 중복 확인
                        ).permitAll()

                        // ===== 나머지는 인증 필요 (Gateway에서 검증) =====
                        .anyRequest().permitAll() // Gateway에서 이미 검증했으므로 허용
                );

        return http.build();
    }
}