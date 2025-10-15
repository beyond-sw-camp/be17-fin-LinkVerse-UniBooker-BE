package org.example.unibooker.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7); // "Bearer " 제거

        try {
            // 2. 토큰 검증
            if (jwtUtil.validateToken(token)) {
                // 3. 토큰에서 사용자 정보 추출
                Long userId = jwtUtil.getUserId(token);       // 기존 메서드 사용
                String email = jwtUtil.getEmail(token);       // 기존 메서드 사용
                String role = jwtUtil.getRole(token);         // 새로 추가한 메서드 사용

                // 4. Spring Security 인증 객체 생성
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,      // principal - @AuthenticationPrincipal로 받음
                                null,        // credentials
                                authorities  // 권한
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 5. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 - userId: {}, email: {}, role: {}",
                        userId, email, role);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}