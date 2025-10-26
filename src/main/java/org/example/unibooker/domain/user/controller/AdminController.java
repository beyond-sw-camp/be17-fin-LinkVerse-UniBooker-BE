package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import org.example.unibooker.domain.user.service.AuthService;
import org.example.unibooker.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.AdminDto;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.dto.ManagerDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.AdminService;
import org.example.unibooker.domain.user.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 관리자 및 매니저 관리 컨트롤러
 * - 관리자 본인 관리 (회원가입 신청, 상태 조회)
 * - 매니저 관리 (생성, 조회, 삭제) - ADMIN 권한
 * - 관리자 관리 (목록 조회, 상태 변경) - SUPER_ADMIN 권한
 */
@Slf4j
@Tag(name = "Admin API", description = "관리자 및 매니저 관리 API")
@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AuthService authService;

    // ========== 관리자 본인 관리 ==========

    /**
     * 관리자 회원가입 신청
     * - 슈퍼 관리자의 승인 필요
     */
    @Operation(summary = "관리자 회원가입 신청",
            description = "기업 관리자 회원가입을 신청합니다. 슈퍼 관리자의 승인이 필요합니다.")
    @PostMapping("/signup")
    public BaseResponse<AdminDto.SignUpResponse> adminSignUp(
            @RequestPart("data") @Valid AdminDto.SignUpRequest request,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile) {

        AdminDto.SignUpResponse response = adminService.signUpAdmin(request, logoFile);
        return BaseResponse.success(response);
    }

    /**
     * 관리자 회원가입 승인 상태 조회
     */
    @Operation(summary = "관리자 회원가입 승인 상태 조회",
            description = "이메일로 관리자 회원가입 승인 상태를 조회합니다.")
    @GetMapping("/status")
    public BaseResponse<AdminDto.StatusResponse> checkAdminStatus(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        AdminDto.StatusResponse response = adminService.checkSignUpStatus(email);
        return BaseResponse.success(response);
    }

    /**
     * 관리자 로그인
     * - 단일 세션 정책: 기존 모든 역할의 쿠키 삭제 후 새 쿠키 생성
     * - JWT 토큰을 HTTP-Only 쿠키로 설정
     */
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid AdminDto.AdminLoginRequest request,
            HttpServletResponse response) {

        // 1. 로그인 처리 (토큰 포함)
        UserDto.LoginResponseWithToken loginResponseWithToken = adminService.adminLogin(request);

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
     * 로그아웃
     * - UserService의 공통 로그아웃 로직 사용
     */
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin,  // 타입 변경: Long → AuthDto.AuthAdmin
            HttpServletResponse response) {

        AuthDto.LogoutResponse logoutResponse = authService.logout(authAdmin.getId());  // authAdmin.getId() 사용

        CookieUtil.deleteAllTokenCookies(response, authAdmin.getRole());  // 권한 파라미터 추가

        return BaseResponse.success(logoutResponse);
    }

    /**
     * 비밀번호 재설정 (첫 로그인 시 필수)
     */
    @PatchMapping("/password/reset")
    public BaseResponse<AdminDto.PasswordResetResponse> resetPassword(
            @RequestBody @Valid AdminDto.PasswordResetRequest request,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        AdminDto.PasswordResetResponse response = adminService.resetPassword(authAdmin.getId(), request);
        return BaseResponse.success(response);
    }

    /**
     * 내 프로필 조회
     */
    @GetMapping("/me")
    public BaseResponse<UserDto.ProfileResponse> getMyProfile(
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin,
            HttpServletRequest request) {

        UserDto.ProfileResponse response = userService.getMyProfile(authAdmin.getId());
        return BaseResponse.success(response);
    }

    /**
     * 내 프로필 수정
     * - UserService의 공통 프로필 수정 로직 사용
     */
    @Operation(summary = "내 프로필 수정",
            description = "현재 로그인한 관리자의 프로필 정보를 수정합니다.")
    @PatchMapping("/me")
    public BaseResponse<UserDto.ProfileResponse> updateMyProfile(
            @RequestBody @Valid UserDto.ProfileUpdateRequest request,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        UserDto.ProfileResponse response = userService.updateMyProfile(authAdmin.getId(), request);
        return BaseResponse.success(response);
    }

    /**
     * 회원 탈퇴
     * - UserService의 공통 회원 탈퇴 로직 사용
     */
    @Operation(summary = "회원 탈퇴",
            description = "현재 로그인한 관리자의 계정을 탈퇴 처리합니다.")
    @DeleteMapping("/me")
    public BaseResponse<UserDto.WithdrawResponse> withdraw(
            @RequestBody @Valid UserDto.WithdrawRequest request,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        UserDto.WithdrawResponse response = userService.withdraw(authAdmin.getId(), request);
        return BaseResponse.success(response);
    }

    /**
     * 이메일 중복 확인
     * - ADMIN/MANAGER 이메일과만 중복 체크
     */
    @Operation(summary = "이메일 중복 확인",
            description = "이메일이 이미 사용 중인지 확인합니다. true: 사용 중, false: 사용 가능")
    @GetMapping("/check-email")
    public BaseResponse<Boolean> checkEmail(
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        boolean exists = userService.existsByEmailForAdmin(email);
        return BaseResponse.success(exists);
    }

    // ========== 매니저 관리 (ADMIN 권한) ==========

    /**
     * 매니저 목록 조회
     * - ADMIN 권한 필요
     */
    @Operation(summary = "매니저 목록 조회",
            description = "현재 관리자의 기업에 소속된 매니저 목록을 조회합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/managers")
    public BaseResponse<ManagerDto.ManagerListResponse> getManagers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        ManagerDto.ManagerListResponse response = adminService.getManagers(authAdmin.getId(), page, size);
        return BaseResponse.success(response);
    }

    /**
     * 매니저 계정 생성
     * - ADMIN 권한 필요
     */
    @Operation(summary = "매니저 계정 생성",
            description = "관리자가 매니저 계정을 생성합니다. 생성된 계정 정보는 이메일로 발송됩니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/managers")
    public BaseResponse<ManagerDto.CreateResponse> createManager(
            @RequestBody @Valid ManagerDto.CreateRequest request,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        ManagerDto.CreateResponse response = adminService.createManager(request, authAdmin.getId());
        return BaseResponse.success(response);
    }

    /**
     * 매니저 계정 삭제
     * - ADMIN 권한 필요
     */
    @Operation(summary = "매니저 계정 삭제",
            description = "관리자가 매니저 계정을 삭제합니다. (ADMIN 권한 필요)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/managers/{managerId}")
    public BaseResponse<ManagerDto.ManagerDeleteResponse> deleteManager(
            @PathVariable Long managerId,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        if (authAdmin == null) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED);
        }

        ManagerDto.ManagerDeleteResponse response = adminService.deleteManager(managerId, authAdmin.getId());
        return BaseResponse.success(response);
    }

    // ========== 관리자 관리 (SUPER_ADMIN 권한) ==========

    /**
     * 관리자+매니저 목록 조회
     * - SUPER_ADMIN 권한 필요
     */
    @Operation(summary = "관리자+매니저 목록 조회",
            description = "전체 관리자와 매니저 목록을 조회합니다. (SUPER_ADMIN 권한 필요)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public BaseResponse<AdminDto.AdminListResponse> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        // String → Enum 변환
        UserRole userRole = null;
        if (role != null && !role.isBlank()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_USER_ROLE);
            }
        }

        UserStatus userStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_USER_STATUS);
            }
        }

        AdminDto.AdminListResponse response = adminService.getAllAdmins(page, size, userRole, userStatus);
        return BaseResponse.success(response);
    }

    /**
     * 관리자/매니저 상태 변경
     * - SUPER_ADMIN 권한 필요
     */
    @Operation(summary = "관리자/매니저 상태 변경",
            description = "관리자 또는 매니저의 계정 상태를 변경합니다. (SUPER_ADMIN 권한 필요)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{userId}/status")
    public BaseResponse<String> updateAdminStatus(
            @PathVariable Long userId,
            @RequestBody @Valid AdminDto.AdminStatusUpdateRequest request) {

        adminService.updateAdminStatus(userId, request);
        return BaseResponse.success("계정 상태가 성공적으로 변경되었습니다.");
    }
}