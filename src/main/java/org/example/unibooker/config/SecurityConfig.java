package org.example.unibooker.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.config.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정
 * - JWT 인증 필터 등록
 * - CORS 설정
 * - 권한별 접근 제어
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"code\":40100,\"message\":\"인증이 필요합니다.\",\"isSuccess\":false}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // ===== 회원가입 =====
                        .requestMatchers(HttpMethod.POST, "/api/users/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admins/signup").permitAll()

                        // ===== 로그인 =====
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admins/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/super/login").permitAll()

                        // ===== 토큰 갱신 (공통) =====
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                        // ===== 로그아웃 =====
                        .requestMatchers(HttpMethod.POST, "/api/users/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/admins/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/super/logout").authenticated()

                        // ===== 상태 조회 =====
                        .requestMatchers(HttpMethod.GET, "/api/admins/status").permitAll()

                        // ===== 중복 확인 =====
                        .requestMatchers(HttpMethod.GET, "/api/users/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admins/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/check-slug").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/companies/check-business-number").permitAll()

                        // ===== 비밀번호 찾기 =====
                        .requestMatchers(HttpMethod.POST, "/api/users/reset-password").permitAll()

                        // ===== 아이디 찾기 ===== (추가)
                        .requestMatchers(HttpMethod.POST, "/api/users/find-email").permitAll()

                        // ===== 계정 조회 (아이디 찾기) =====
                        .requestMatchers(HttpMethod.GET, "/api/users/accounts").permitAll()

                        // ===== 기업 정보 조회 =====
                        .requestMatchers(HttpMethod.GET, "/api/companies/slug/**").permitAll()

                        // ===== 프로필 관리 (인증 필요) =====
                        .requestMatchers(HttpMethod.GET, "/api/admins/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/admins/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/admins/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/profile").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/profile").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/profile").authenticated()

                        // ===== 정적 리소스 =====
                        .requestMatchers("/uploads/**").permitAll()

                        // ===== Swagger =====
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // ===== 슈퍼 관리자 전용 경로 (인증 필요) =====
                        .requestMatchers(HttpMethod.GET, "/api/companies").hasRole("SUPER")
                        .requestMatchers(HttpMethod.GET, "/api/companies/pending").hasRole("SUPER")
                        .requestMatchers(HttpMethod.GET, "/api/companies/{companyId}").hasRole("SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/companies/{companyId}/approve").hasRole("SUPER")
                        .requestMatchers(HttpMethod.POST, "/api/companies/{companyId}/reject").hasRole("SUPER")
                        .requestMatchers(HttpMethod.PATCH, "/api/companies/{companyId}/status").hasRole("SUPER")

                        // ===== 리소스 관련 경로 =====
                        .requestMatchers("/api/resource-group/**").authenticated()


                        // ===== 이미지 업로드 관련 경로 =====
                        .requestMatchers("/api/image-upload").authenticated()

                        .requestMatchers("/ws/**").permitAll() // WebSocket 엔드포인트 허용

                        // ===== actuator ====
                        .requestMatchers("/actuator/**").authenticated()


                        // ===== 그 외 모든 요청은 인증 필요 =====
                        .anyRequest().authenticated()
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * CORS 설정
     * - 프론트엔드(localhost:5173) 요청 허용
     * - 쿠키 전송을 위한 credentials 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 URL)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://www.unibooker.kro.kr:4040",
                "https://www.unibooker.kro.kr:443",
                "http://www.unibooker.kro.kr:80"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보(쿠키 등) 허용 - 쿠키 기반 인증에 필수!
        configuration.setAllowCredentials(true);

        // 클라이언트에 노출할 헤더 (선택)
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie"));

        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}