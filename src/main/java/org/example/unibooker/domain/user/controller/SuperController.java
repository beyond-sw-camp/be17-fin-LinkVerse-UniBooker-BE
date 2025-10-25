package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.user.model.dto.SuperDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.SuperService;
import org.example.unibooker.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 슈퍼 관리자 컨트롤러
 * - 슈퍼 관리자 로그인/로그아웃만 처리
 * - 실제 관리 기능은 AdminController의 /api/admins 엔드포인트 사용
 */
@Tag(name = "Super Admin API", description = "슈퍼 관리자 로그인/로그아웃 API")
@RestController
@RequestMapping("/api/super")
@RequiredArgsConstructor
public class SuperController {

    private final SuperService superService;
    private final UserService userService;

    // ========== 슈퍼 관리자 로그인/로그아웃 ==========

    /**
     * 슈퍼 관리자 로그인
     * - Refresh Token은 HttpOnly Cookie에 저장
     */
    @Operation(summary = "슈퍼 관리자 로그인",
            description = "슈퍼 관리자 이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid SuperDto.SuperLoginRequest request,
            HttpServletResponse response) {

        UserDto.LoginResponse loginResponse = superService.superLogin(request);

        // Access Token을 HttpOnly Cookie에 저장
        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);  // 개발: false, 운영: true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);  // 30분
        response.addCookie(accessTokenCookie);

        // Refresh Token을 HttpOnly Cookie에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);  // 개발: false, 운영: true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7일
        response.addCookie(refreshTokenCookie);

        return BaseResponse.success(loginResponse);
    }

    /**
     * 슈퍼 관리자 로그아웃
     * - Refresh Token 무효화
     * - 쿠키 삭제 추가
     */
    @Operation(summary = "슈퍼 관리자 로그아웃",
            description = "현재 로그인 세션을 종료하고 Refresh Token을 무효화합니다.")
    @PostMapping("/logout")
    public BaseResponse<UserDto.LogoutResponse> logout(
            @RequestBody @Valid UserDto.LogoutRequest request,
            @AuthenticationPrincipal Long userId, HttpServletResponse response) {

        UserDto.LogoutResponse logoutResponse = userService.logout(userId, request);

        // ===== 쿠키 삭제 로직 추가 =====

        // Access Token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        return BaseResponse.success(logoutResponse);
    }
}