package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.dto.SuperDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.AuthService;
import org.example.unibooker.domain.user.service.SuperService;
import org.example.unibooker.utils.CookieUtil;
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
    private final AuthService authService;  // ← 추가

    // ========== 슈퍼 관리자 로그인/로그아웃 ==========

    /**
     * 슈퍼 관리자 로그인
     * - 단일 세션 정책: 기존 모든 역할의 쿠키 삭제 후 새 쿠키 생성
     * - Access Token과 Refresh Token을 모두 HttpOnly Cookie에 저장
     */
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid SuperDto.SuperLoginRequest request,
            HttpServletResponse response) {

        // 1. 로그인 처리 (토큰 포함)
        UserDto.LoginResponseWithToken loginResponseWithToken = superService.superLogin(request);

        // 2. 단일 세션 정책: 모든 역할의 기존 쿠키 삭제
        CookieUtil.deleteAllRolesCookies(response);

        // 3. 현재 역할의 토큰을 HttpOnly Cookie에 저장
        response.addCookie(CookieUtil.createAccessTokenCookie(
                loginResponseWithToken.getAccessToken(),
                loginResponseWithToken.getRole()
        ));
        response.addCookie(CookieUtil.createRefreshTokenCookie(
                loginResponseWithToken.getRefreshToken(),
                loginResponseWithToken.getRole()
        ));

        // 4. 클라이언트 응답 생성 (토큰 제외)
        return BaseResponse.success(loginResponseWithToken.toResponse());
    }

    /**
     * 슈퍼 관리자 로그아웃
     * - Refresh Token 삭제
     * - Access Token과 Refresh Token 쿠키 삭제
     */
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin,  // 타입 변경: Long → AuthDto.AuthAdmin
            HttpServletResponse response) {

        // Refresh Token 삭제
        AuthDto.LogoutResponse logoutResponse = authService.logout(authAdmin.getId());  // authAdmin.getId() 사용

        CookieUtil.deleteAllTokenCookies(response, authAdmin.getRole());  // 권한 파라미터 추가

        return BaseResponse.success(logoutResponse);
    }

    // ========== 관리자 관리 ==========

    /**
     * 관리자 상태 변경 (ACTIVE ↔ SUSPENDED)
     */
    @Operation(summary = "관리자 상태 변경",
            description = "관리자(ADMIN, MANAGER)의 상태를 변경합니다. (SUPER 권한 필요)")
    @PatchMapping("/managers/{userId}/status")
    public BaseResponse<SuperDto.ManagerStatusUpdateResponse> updateManagerStatus(
            @PathVariable Long userId,
            @RequestBody @Valid SuperDto.ManagerStatusUpdateRequest request) {

        SuperDto.ManagerStatusUpdateResponse response =
                superService.updateManagerStatus(userId, request.getStatus());

        return BaseResponse.success(response);
    }
}