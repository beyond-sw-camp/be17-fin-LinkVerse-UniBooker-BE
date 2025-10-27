package org.example.unibooker.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.example.unibooker.utils.CookieUtil;
import org.example.unibooker.domain.user.model.UserRole;

import java.io.IOException;
import java.util.List;

/**
 * JWT 인증 필터
 * - 쿠키에서 accessToken 추출
 * - 토큰 유효성 검증
 * - SecurityContext에 인증 정보 저장
 * - companyId 검증 (기업별 리소스 접근 제어)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 쿠키에서 accessToken 추출
        String token = extractTokenFromCookie(request);

        // 토큰이 없으면 다음 필터로
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. 토큰 검증
            if (jwtUtil.validateToken(token)) {
                // 3. 토큰에서 사용자 정보 추출
                Long userId = jwtUtil.getUserId(token);
                String email = jwtUtil.getEmail(token);
                String role = jwtUtil.getRole(token);
                Long tokenCompanyId = jwtUtil.getCompanyId(token);

                // 4. URL에서 companySlug 추출 및 검증
                String requestUri = request.getRequestURI();

                // 기업별 리소스 접근 검증 (/api/c/{companySlug}/... 패턴)
                if (requestUri.matches("^/api/c/[a-z0-9-]+/.*")) {
                    // companySlug가 URL에 포함된 경우
                    if (tokenCompanyId == null) {
                        log.warn("기업별 리소스 접근 시도하나 토큰에 companyId 없음 - userId: {}", userId);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":50010,\"message\":\"해당 기업의 리소스에 접근 권한이 없습니다.\",\"isSuccess\":false}");
                        return;
                    }

                    // TODO: companySlug로 실제 companyId 조회하여 tokenCompanyId와 비교
                    // CompanyRepository에서 slug로 조회 후 ID 비교 로직 추가 필요
                    log.debug("기업별 리소스 접근 - companyId: {}", tokenCompanyId);
                }

                // 5. DB에서 사용자 정보 조회
                Users user = userRepository.findById(userId).orElse(null);

                if (user == null) {
                    log.warn("JWT 토큰의 userId에 해당하는 사용자 없음 - userId: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 6. 역할에 따라 AuthDto 객체 생성
                Object principal = createPrincipal(user);

                // 7. Spring Security 인증 객체 생성
                List<SimpleGrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,   // AuthDto 객체 저장
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공 - userId: {}, email: {}, role: {}, companyId: {}, principalType: {}",
                        userId, email, role, tokenCompanyId, principal.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 요청 경로를 기반으로 권한 추론
     * - 프론트엔드 경로: /admin, /manager, /c/, /super
     * - API 경로: /api/admins, /api/managers, /api/users, /api/super
     */
    private UserRole inferRoleFromPath(String requestUri) {
        // 프론트엔드 경로
        if (requestUri.startsWith("/admin")) {
            return UserRole.ADMIN;
        } else if (requestUri.startsWith("/manager")) {
            return UserRole.MANAGER;
        } else if (requestUri.startsWith("/c/")) {
            return UserRole.USER;
        } else if (requestUri.startsWith("/super")) {
            return UserRole.SUPER;
        }

        // API 경로
        if (requestUri.startsWith("/api/admins")) {
            return UserRole.ADMIN;
        } else if (requestUri.startsWith("/api/managers")) {
            return UserRole.MANAGER;
        } else if (requestUri.startsWith("/api/users") || requestUri.startsWith("/api/c/")) {
            return UserRole.USER;
        } else if (requestUri.startsWith("/api/super")) {
            return UserRole.SUPER;
        }

        return null;
    }

    /**
     * 쿠키에서 권한별 accessToken 추출
     * - 경로 기반으로 권한을 추론하여 해당 쿠키 탐색
     * - API 경로는 모든 권한 순차 확인
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String requestUri = request.getRequestURI();
        UserRole inferredRole = inferRoleFromPath(requestUri);

        // 특정 권한 경로인 경우 (예: /admin, /manager, /c/, /super)
        if (inferredRole != null) {
            String targetCookieName = CookieUtil.getAccessTokenCookieName(inferredRole);
            for (Cookie cookie : cookies) {
                if (targetCookieName.equals(cookie.getName())) {
                    log.debug("경로 기반 토큰 추출 - role: {}, cookieName: {}",
                            inferredRole, targetCookieName);
                    return cookie.getValue();
                }
            }
            return null;
        }

        // API 경로 등: 모든 권한 쿠키 순차 확인
        for (UserRole role : UserRole.values()) {
            String cookieName = CookieUtil.getAccessTokenCookieName(role);
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    log.debug("API 경로 토큰 추출 - role: {}, cookieName: {}",
                            role, cookieName);
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 역할에 따라 적절한 AuthDto 객체 생성
     */
    private Object createPrincipal(Users user) {
        return switch (user.getRole()) {
            case USER -> AuthDto.AuthUser.from(user);
            case MANAGER -> AuthDto.AuthManager.from(user);
            case ADMIN, SUPER -> AuthDto.AuthAdmin.from(user);
            default -> {
                log.error("알 수 없는 역할 - userId: {}, role: {}", user.getId(), user.getRole());
                throw new IllegalStateException("알 수 없는 사용자 역할: " + user.getRole());
            }
        };
    }
}