package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.exception.RefreshTokenException;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.service.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * 공통 인증 컨트롤러
 * - 모든 권한의 토큰 갱신을 통합 처리
 * - /api/auth/* 경로로 통일
 */
@Tag(name = "Auth API", description = "공통 인증 API (토큰 갱신)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Access Token 갱신 (공통)
     * - 모든 권한(USER, ADMIN, MANAGER, SUPER) 공통 사용
     * - Refresh Token을 사용하여 새로운 Access Token 발급
     * - 쿠키에서 Refresh Token 자동 추출
     */
    @Operation(summary = "Access Token 갱신 (공통)",
            description = "Refresh Token을 사용하여 만료된 Access Token을 갱신합니다. 모든 권한에서 공통으로 사용합니다.")
    @PostMapping("/refresh")
    public BaseResponse<AuthDto.RefreshTokenResponse> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // Cookie에서 Refresh Token 없으면 에러
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenException.RefreshTokenNotFoundException();
        }

        // Access Token 갱신
        AuthDto.RefreshTokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);

        // 새로운 Access Token을 HttpOnly Cookie에 저장
        Cookie accessTokenCookie = new Cookie("accessToken", tokenResponse.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false);  // 개발: false, 운영: true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);  // 15분

        response.addCookie(accessTokenCookie);

        // (선택) Refresh Token Rotation 적용 시
        // if (tokenResponse.getRefreshToken() != null) {
        //     Cookie refreshTokenCookie = new Cookie("refreshToken", tokenResponse.getRefreshToken());
        //     refreshTokenCookie.setHttpOnly(true);
        //     refreshTokenCookie.setSecure(false);
        //     refreshTokenCookie.setPath("/");
        //     refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        //     response.addCookie(refreshTokenCookie);
        // }

        return BaseResponse.success(tokenResponse);
    }
}