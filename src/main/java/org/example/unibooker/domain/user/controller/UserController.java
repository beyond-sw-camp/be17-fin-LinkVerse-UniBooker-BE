package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 일반 사용자 회원 관리 컨트롤러
 * - 회원가입, 로그인, 비밀번호 변경, 프로필 관리, 회원 탈퇴
 */
@Tag(name = "User API", description = "일반 사용자 회원 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== 회원가입 ==========

    /**
     * 일반 사용자 회원가입
     */
    @Operation(summary = "일반 사용자 회원가입",
            description = "일반 사용자(고객) 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public BaseResponse<UserDto.SignUpResponse> signUp(
            @RequestBody @Valid UserDto.SignUpRequest request) {

        UserDto.SignUpResponse response = userService.signUpUser(request);
        return BaseResponse.success(response);
    }

    // ========== 로그인 ==========

    /**
     * 로그인 처리
     * - Refresh Token은 HttpOnly Cookie에 저장
     */
    @Operation(summary = "로그인",
            description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid UserDto.LoginRequest request,
            HttpServletResponse response) {

        UserDto.LoginResponse loginResponse = userService.login(request);

        // Refresh Token을 HttpOnly Cookie에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);   // JavaScript 접근 불가 (XSS 방어)
        refreshTokenCookie.setSecure(true);     // HTTPS만 전송
        refreshTokenCookie.setPath("/");        // 모든 경로에서 사용
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7일 (초 단위)

        response.addCookie(refreshTokenCookie);

        return BaseResponse.success(loginResponse);
    }

    // ========== 로그아웃 (신규) ==========

    /**
     * 로그아웃 처리
     * - Refresh Token 무효화
     */
    @Operation(summary = "로그아웃",
            description = "현재 로그인 세션을 종료하고 Refresh Token을 무효화합니다.")
    @PostMapping("/logout")
    public BaseResponse<UserDto.LogoutResponse> logout(
            @RequestBody @Valid UserDto.LogoutRequest request,
            @AuthenticationPrincipal Long userId) {

        UserDto.LogoutResponse response = userService.logout(userId, request);
        return BaseResponse.success(response);
    }

    // ========== 비밀번호 변경 ==========

    /**
     * 비밀번호 변경
     */
    @Operation(summary = "비밀번호 변경",
            description = "사용자 비밀번호를 변경합니다. 첫 로그인 시 필수입니다.")
    @PutMapping("/password")
    public BaseResponse<String> changePassword(
            @RequestBody @Valid UserDto.PasswordChangeRequest request,
            @AuthenticationPrincipal Long userId) {

        userService.changePassword(userId, request);
        return BaseResponse.success("비밀번호가 성공적으로 변경되었습니다.");
    }

    // ========== 프로필 관리 ==========

    /**
     * 내 프로필 조회
     */
    @Operation(summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public BaseResponse<UserDto.ProfileResponse> getMyProfile(
            @AuthenticationPrincipal Long userId) {

        UserDto.ProfileResponse response = userService.getMyProfile(userId);
        return BaseResponse.success(response);
    }

    /**
     * 내 프로필 수정
     */
    @Operation(summary = "내 프로필 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    @PatchMapping("/profile")
    public BaseResponse<UserDto.ProfileResponse> updateMyProfile(
            @RequestBody @Valid UserDto.ProfileUpdateRequest request,
            @AuthenticationPrincipal Long userId) {

        UserDto.ProfileResponse response = userService.updateMyProfile(userId, request);
        return BaseResponse.success(response);
    }

    // ========== 회원 탈퇴 (신규) ==========

    /**
     * 회원 탈퇴
     * - 계정 상태를 DELETED로 변경
     */
    @Operation(summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다. 계정 상태가 DELETED로 변경됩니다.")
    @DeleteMapping("/profile")
    public BaseResponse<UserDto.WithdrawResponse> withdraw(
            @RequestBody @Valid UserDto.WithdrawRequest request,
            @AuthenticationPrincipal Long userId) {

        UserDto.WithdrawResponse response = userService.withdraw(userId, request);
        return BaseResponse.success(response);
    }

    // ========== 이메일 중복 확인 ==========

    /**
     * 이메일 중복 확인
     */
    @Operation(summary = "이메일 중복 확인",
            description = "이메일이 이미 사용 중인지 확인합니다. true: 사용 중, false: 사용 가능")
    @GetMapping("/check-email")
    public BaseResponse<Boolean> checkEmail(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        boolean exists = userService.existsByEmail(email);
        return BaseResponse.success(exists);
    }
}