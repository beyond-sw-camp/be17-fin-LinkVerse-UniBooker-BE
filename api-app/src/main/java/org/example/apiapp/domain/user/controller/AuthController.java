package org.example.apiapp.domain.user.controller;

import org.example.common.util.CookieUtil;
import org.example.common.model.UserRole;
import org.example.apiapp.domain.user.model.dto.UserDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.apiapp.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 공통 인증 컨트롤러
 * - 모든 권한(USER, ADMIN, MANAGER, SUPER)에서 사용하는 공통 인증 기능
 * - 토큰 갱신
 *
 * [변경 이력]
 * - 권한별 로그인/회원가입 기능은 각 권한별 Controller로 분리
 *   (UserController, AdminController, SuperController)
 * - 토큰 갱신 기능만 공통 기능으로 유지
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 토큰 갱신 (모든 권한 공통)
     * - 쿠키에서 refreshToken 추출
     * - 갱신된 토큰을 쿠키에 저장
     * - Response Body에는 토큰 제외
     */
    @PostMapping("/refresh")
    public ResponseEntity<UserDto.LoginResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("POST /api/auth/refresh - 토큰 갱신");

        // 1. 쿠키에서 refreshToken 추출 (모든 권한 순차 확인)
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            log.warn("Refresh Token이 쿠키에 없음");
            return ResponseEntity.status(401).build();
        }

        // 2. 토큰 갱신 (Service에서 토큰 포함 DTO 반환)
        UserDto.LoginResponseWithToken loginResponseWithToken =
                authService.refreshTokenWithRole(refreshToken);

        // 3. 갱신된 토큰을 쿠키에 저장
        response.addCookie(CookieUtil.createAccessTokenCookie(
                loginResponseWithToken.getAccessToken(),
                loginResponseWithToken.getRole()
        ));
        response.addCookie(CookieUtil.createRefreshTokenCookie(
                loginResponseWithToken.getRefreshToken(),
                loginResponseWithToken.getRole()
        ));

        // 4. Response Body에는 토큰 제외
        UserDto.LoginResponse loginResponse = UserDto.LoginResponse.builder()
                .userId(loginResponseWithToken.getUserId())
                .email(loginResponseWithToken.getEmail())
                .name(loginResponseWithToken.getName())
                .role(loginResponseWithToken.getRole())
                .companyId(loginResponseWithToken.getCompanyId())
                .companySlug(loginResponseWithToken.getCompanySlug())
                .passwordChangeRequired(loginResponseWithToken.getIsFirstLogin())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 쿠키에서 refreshToken 추출 (모든 권한 순차 확인)
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        // 모든 권한의 refreshToken 쿠키 순차 확인
        for (UserRole role : UserRole.values()) {
            String cookieName = CookieUtil.getRefreshTokenCookieName(role);
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    log.debug("RefreshToken 쿠키 발견 - role: {}, cookieName: {}", role, cookieName);
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}