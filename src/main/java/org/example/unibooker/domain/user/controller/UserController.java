package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.exception.RefreshTokenException;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.service.CompanyService;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.UserService;
import org.example.unibooker.domain.user.service.AuthService;
import org.example.unibooker.utils.CookieUtil;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final AuthService authService;

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
     * 일반 사용자 로그인
     * - 단일 세션 정책: 기존 모든 역할의 쿠키 삭제 후 새 쿠키 생성
     * - Access Token과 Refresh Token을 모두 HttpOnly Cookie에 저장
     */
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid UserDto.LoginRequest request,
            HttpServletResponse response) {

        // 1. 로그인 처리 (토큰 포함)
        UserDto.LoginResponseWithToken loginResponseWithToken = userService.login(request);

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

    // ========== 로그아웃 ==========

    /**
     * 로그아웃 처리
     * - Refresh Token 삭제
     * - Access Token과 Refresh Token 쿠키 삭제
     */
    @Operation(summary = "로그아웃",
            description = "현재 로그인 세션을 종료하고 토큰을 삭제합니다.")
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @AuthenticationPrincipal AuthDto.AuthUser authUser,  // 타입 변경: Long → AuthDto.AuthUser
            HttpServletResponse response) {

        // Refresh Token 삭제
        AuthDto.LogoutResponse logoutResponse = authService.logout(authUser.getId());  // authUser.getId() 사용

        CookieUtil.deleteAllTokenCookies(response, authUser.getRole());  // 권한 파라미터 추가

        return BaseResponse.success(logoutResponse);
    }

    // ========== 인증 확인 ==========

    /**
     * 현재 로그인한 사용자 정보 조회
     * - 인증 상태 확인 및 기본 정보 반환
     * - 프론트엔드에서 실제 쿠키 기반 인증 검증용
     */
    @Operation(summary = "현재 사용자 정보 조회",
            description = "JWT 쿠키 기반으로 현재 로그인한 사용자의 기본 정보를 반환합니다. 인증 확인용으로 사용됩니다.")
    @GetMapping("/me")
    public BaseResponse<UserDto.CurrentUserResponse> getCurrentUser(
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {

        UserDto.CurrentUserResponse response = userService.getCurrentUserInfo(authUser.getId());
        return BaseResponse.success(response);
    }

    // ========== Refresh Token 갱신 ==========

    /**
     * Access Token 갱신
     * - Refresh Token을 사용하여 새로운 Access Token 발급
     */
    @Operation(summary = "Access Token 갱신",
            description = "Refresh Token을 사용하여 만료된 Access Token을 갱신합니다.")
    @PostMapping("/refresh")
    public BaseResponse<AuthDto.RefreshTokenResponse> refreshToken(
            @CookieValue(value = "userRefreshToken", required = false) String refreshToken,  // ← 수정: refreshToken → userRefreshToken
            HttpServletResponse response) {

        // Cookie에서 Refresh Token 없으면 에러
        if (refreshToken == null) {
            throw new RefreshTokenException.RefreshTokenNotFoundException();
        }

        // Access Token 갱신 (role 정보 포함)
        AuthDto.RefreshTokenResponseWithToken tokenResponse = authService.refreshAccessToken(refreshToken);

        // 새로운 Access Token을 HttpOnly Cookie에 저장 (role 기반)
        response.addCookie(CookieUtil.createAccessTokenCookie(
                tokenResponse.getAccessToken(),
                tokenResponse.getRole()  // ← 이제 컴파일 에러 해결
        ));

        return BaseResponse.success(tokenResponse.toResponse());
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
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {  // ← 수정: Long userId → AuthDto.AuthUser authUser

        userService.changePassword(authUser.getId(), request);  // ← 수정: userId → authUser.getId()
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
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {  // ← 수정: Long userId → AuthDto.AuthUser authUser

        UserDto.ProfileResponse response = userService.getMyProfile(authUser.getId());  // ← 수정
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
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {  // ← 수정: Long userId → AuthDto.AuthUser authUser

        UserDto.ProfileResponse response = userService.updateMyProfile(authUser.getId(), request);  // ← 수정
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
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {  // ← 수정: Long userId → AuthDto.AuthUser authUser

        UserDto.WithdrawResponse response = userService.withdraw(authUser.getId(), request);  // ← 수정
        return BaseResponse.success(response);
    }

    // ========== 이메일 중복 확인 (기업별) ==========

    /**
     * 이메일 중복 확인 (특정 기업 내에서)
     */
    @Operation(summary = "이메일 중복 확인 (기업별)",
            description = "특정 기업 내에서 이메일이 이미 사용 중인지 확인합니다. " +
                    "true: 해당 기업에서 사용 중, false: 해당 기업에서 사용 가능")
    @GetMapping("/check-email")
    public BaseResponse<Boolean> checkEmail(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email,
            @RequestParam @Schema(description = "기업 ID", example = "1") Long companyId) {

        boolean exists = userService.existsByEmailAndCompany(email, companyId);
        return BaseResponse.success(exists);
    }

    // ========== 이메일로 가입한 모든 기업 조회 (추가) ==========

    /**
     * 이메일로 가입한 기업 목록 조회
     */
    @Operation(summary = "이메일로 가입한 기업 목록 조회",
            description = "해당 이메일로 가입한 모든 기업의 계정 정보를 조회합니다.")
    @GetMapping("/accounts")
    public BaseResponse<List<UserDto.AccountInfo>> getAccountsByEmail(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        List<UserDto.AccountInfo> accounts = userService.getAccountsByEmail(email);
        return BaseResponse.success(accounts);
    }

    // ========== 비밀번호 찾기 ==========

    /**
     * 비밀번호 찾기 - 임시 비밀번호 발급
     */
    @Operation(summary = "비밀번호 찾기",
            description = "이메일과 기업ID로 사용자를 확인하고 임시 비밀번호를 이메일로 발송합니다.")
    @PostMapping("/reset-password")
    public BaseResponse<String> resetPassword(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email,
            @RequestParam @Schema(description = "기업 ID", example = "1") Long companyId) {

        userService.resetPassword(email, companyId);
        return BaseResponse.success("임시 비밀번호가 이메일로 발송되었습니다. 이메일을 확인해주세요.");
    }

    // ========== 아이디 찾기 ==========

    /**
     * 아이디 찾기 - 이메일 조회
     */
    @Operation(summary = "아이디 찾기",
            description = "이름과 전화번호 또는 생년월일로 가입한 이메일을 찾습니다. 이메일은 마스킹 처리되어 반환됩니다.")
    @PostMapping("/find-email")
    public BaseResponse<UserDto.FindEmailResponse> findEmail(
            @RequestBody @Valid UserDto.FindEmailRequest request) {

        UserDto.FindEmailResponse response = userService.findEmail(request);
        return BaseResponse.success(response);
    }
}