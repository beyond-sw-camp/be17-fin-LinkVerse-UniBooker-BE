package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.exception.RefreshTokenException;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.service.AuthService;
import org.example.unibooker.utils.CookieUtil;
import org.example.unibooker.domain.user.model.UserRole;
import jakarta.servlet.http.HttpServletRequest;
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
     * Access Token 갱신
     * - 권한별 Refresh Token을 Cookie에서 자동 추출
     * - 요청 경로 기반으로 권한 추론
     * - 새로운 Access Token을 권한별 Cookie로 전달
     */
    @Operation(summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 Access Token을 갱신합니다. 권한별 쿠키에서 자동으로 추출됩니다.")
    @PostMapping("/refresh")
    public BaseResponse<AuthDto.RefreshTokenResponse> refreshToken(
            @CookieValue(value = "adminRefreshToken", required = false) String adminRefreshToken,
            @CookieValue(value = "userRefreshToken", required = false) String userRefreshToken,
            @CookieValue(value = "superRefreshToken", required = false) String superRefreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {

        // 1. 요청 경로 및 Referer 기반 권한 추론
        String requestUri = request.getRequestURI();
        String referer = request.getHeader("Referer");

        UserRole role = inferRoleFromRequest(requestUri, referer);

        // 2. 권한에 맞는 Refresh Token 선택
        String refreshToken = getRefreshTokenByRole(role, adminRefreshToken,
                userRefreshToken, superRefreshToken);

        // 3. Refresh Token 검증
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenException.RefreshTokenNotFoundException();
        }

        // 4. Access Token 갱신
        AuthDto.RefreshTokenResponseWithToken tokenResponseWithToken =
                authService.refreshAccessToken(refreshToken);

        // 5. 권한별 새로운 Access Token 쿠키 생성
        response.addCookie(CookieUtil.createAccessTokenCookie(
                tokenResponseWithToken.getAccessToken(),
                role  // 권한 파라미터 추가
        ));

        // (선택) Refresh Token Rotation 적용 시
        // if (tokenResponseWithToken.getRefreshToken() != null) {
        //     response.addCookie(CookieUtil.createRefreshTokenCookie(
        //             tokenResponseWithToken.getRefreshToken(), role));
        // }

        // 6. 클라이언트 응답 생성 (토큰 제외)
        return BaseResponse.success(tokenResponseWithToken.toResponse());
    }

    /**
     * 요청 정보 기반 권한 추론
     * - Referer 헤더 우선 확인
     * - URI 기반 추론
     * - 기본값: USER
     */
    private UserRole inferRoleFromRequest(String requestUri, String referer) {
        // Referer 헤더 우선 확인 (프론트엔드에서 호출 시)
        if (referer != null) {
            if (referer.contains("/admin")) return UserRole.ADMIN;
            if (referer.contains("/manager")) return UserRole.MANAGER;
            if (referer.contains("/c/")) return UserRole.USER;
            if (referer.contains("/super")) return UserRole.SUPER;
        }

        // URI 기반 추론 (백엔드 직접 호출 시)
        if (requestUri.startsWith("/admin")) return UserRole.ADMIN;
        if (requestUri.startsWith("/manager")) return UserRole.MANAGER;
        if (requestUri.startsWith("/c/")) return UserRole.USER;
        if (requestUri.startsWith("/super")) return UserRole.SUPER;

        // 기본값 (API 경로 등)
        return UserRole.USER;
    }

    /**
     * 권한별 Refresh Token 선택
     * - Manager는 Admin 토큰 사용
     */
    private String getRefreshTokenByRole(UserRole role,
                                         String adminToken,
                                         String userToken,
                                         String superToken) {
        return switch(role) {
            case ADMIN, MANAGER -> adminToken;
            case USER -> userToken;
            case SUPER -> superToken;
        };
    }
}