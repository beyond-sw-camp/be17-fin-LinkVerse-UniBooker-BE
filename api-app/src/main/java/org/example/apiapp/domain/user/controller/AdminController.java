package org.example.apiapp.domain.user.controller;

import org.example.common.util.CookieUtil;
import org.example.common.model.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import org.example.apiapp.domain.user.model.dto.AdminDto;
import org.example.apiapp.domain.user.model.dto.AuthDto;
import org.example.apiapp.domain.user.model.dto.ManagerDto;
import org.example.apiapp.domain.user.model.dto.UserDto;
import org.example.common.exception.BaseException;
import org.example.common.base.BaseResponseStatus;
import org.example.common.base.BaseResponse;
import org.example.apiapp.domain.user.service.AdminService;
import org.example.apiapp.domain.user.service.AuthService;
import org.example.apiapp.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 및 매니저 관리 컨트롤러
 * - 관리자 본인 관리 (회원가입, 로그인, 프로필 등)
 * - 매니저 관리 (생성, 조회, 수정, 삭제)
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

    // ========== 관리자 본인 관리 (10개 API) ==========

    /**
     * 1. 관리자 회원가입 신청
     */
    @Operation(
            summary = "관리자 회원가입 신청",
            description = "기업 관리자 회원가입을 신청합니다. 슈퍼 관리자의 승인이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 신청 완료"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "이메일 또는 사업자등록번호 중복")
            }
    )
    @PostMapping("/signup")
    public BaseResponse<AdminDto.SignUpResponse> adminSignUp(
            @RequestBody @Valid AdminDto.SignUpRequest request) {

        log.info("관리자 회원가입 신청 - email: {}", request.getEmail());
        AdminDto.SignUpResponse response = adminService.signUpAdmin(request);
        return BaseResponse.success(response);
    }

    /**
     * 2. 관리자 회원가입 상태 조회
     */
    @Operation(
            summary = "관리자 회원가입 상태 조회",
            description = "이메일로 관리자 회원가입 승인 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @GetMapping("/status")
    public BaseResponse<AdminDto.StatusResponse> checkAdminStatus(
            @Parameter(description = "이메일", required = true, example = "admin@example.com")
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        log.info("관리자 회원가입 상태 조회 - email: {}", email);
        AdminDto.StatusResponse response = adminService.checkSignUpStatus(email);
        return BaseResponse.success(response);
    }

    /**
     * 3. 관리자 로그인
     */
    @Operation(
            summary = "관리자 로그인",
            description = "관리자 또는 매니저 계정으로 로그인합니다. JWT 토큰을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "계정 비활성화")
            }
    )
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid AdminDto.AdminLoginRequest request,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("관리자 로그인 시도 - email: {}", request.getEmail());

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

        // 4. Response Body에는 토큰 제외한 정보만 반환
        UserDto.LoginResponse loginResponse = UserDto.LoginResponse.builder()
                .userId(loginResponseWithToken.getUserId())
                .email(loginResponseWithToken.getEmail())
                .name(loginResponseWithToken.getName())
                .role(loginResponseWithToken.getRole())
                .companyId(loginResponseWithToken.getCompanyId())
                .companySlug(loginResponseWithToken.getCompanySlug())
                .passwordChangeRequired(loginResponseWithToken.getIsFirstLogin())
                .build();

        return BaseResponse.success(loginResponse);
    }

    /**
     * 3-1. 로그아웃
     */
    @Operation(
            summary = "로그아웃",
            description = "관리자 또는 매니저 로그아웃을 처리합니다. Refresh Token을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String userRole,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("관리자 로그아웃 - userId: {}, role: {}", userId, userRole);

        // 권한 체크: ADMIN 또는 MANAGER만 허용
        if (!"ADMIN".equals(userRole) && !"MANAGER".equals(userRole)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // 1. Refresh Token 삭제 (DB 또는 Redis)
        AuthDto.LogoutResponse logoutResponse = authService.logout(userId);

        // 2. 쿠키 삭제
        UserRole role = UserRole.valueOf(userRole);
        CookieUtil.deleteAllTokenCookies(response, role);

        return BaseResponse.success(logoutResponse);
    }

    /**
     * 3-2. 내 프로필 조회
     */
    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 관리자 또는 매니저의 프로필 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @GetMapping("/me")
    public BaseResponse<UserDto.ProfileResponse> getMyProfile(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("내 프로필 조회 - userId: {}, role: {}", userId, role);

        // 권한 체크: ADMIN 또는 MANAGER만 허용
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        UserDto.ProfileResponse response = userService.getMyProfile(userId);
        return BaseResponse.success(response);
    }

    /**
     * 3-3. 내 프로필 수정
     */
    @Operation(
            summary = "내 프로필 수정",
            description = "현재 로그인한 관리자 또는 매니저의 프로필 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @PatchMapping("/me")
    public BaseResponse<UserDto.ProfileResponse> updateMyProfile(
            @RequestBody @Valid UserDto.ProfileUpdateRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("내 프로필 수정 - userId: {}, role: {}", userId, role);

        // 권한 체크: ADMIN 또는 MANAGER만 허용
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        UserDto.ProfileResponse response = userService.updateMyProfile(userId, request);
        return BaseResponse.success(response);
    }

    /**
     * 3-4. 회원 탈퇴
     */
    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 관리자 또는 매니저의 계정을 탈퇴 처리합니다. 계정 상태가 DELETED로 변경됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
                    @ApiResponse(responseCode = "400", description = "비밀번호 불일치"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @DeleteMapping("/me")
    public BaseResponse<UserDto.WithdrawResponse> withdraw(
            @RequestBody @Valid UserDto.WithdrawRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("회원 탈퇴 - userId: {}, role: {}", userId, role);

        // 권한 체크: ADMIN 또는 MANAGER만 허용
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        UserDto.WithdrawResponse response = userService.withdraw(userId, request);
        return BaseResponse.success(response);
    }

    /**
     * 4. 비밀번호 재설정
     */
    @Operation(
            summary = "비밀번호 재설정",
            description = "관리자 또는 매니저의 비밀번호를 재설정합니다. 첫 로그인 시 필수입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PatchMapping("/password/reset")
    public BaseResponse<AdminDto.PasswordResetResponse> resetPassword(
            @RequestBody @Valid AdminDto.PasswordResetRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("비밀번호 재설정 - userId: {}", userId);
        AdminDto.PasswordResetResponse response = adminService.resetPassword(userId, request);
        return BaseResponse.success(response);
    }

    /**
     * 5. 기업 로고 업데이트
     */
    @Operation(
            summary = "기업 로고 업데이트",
            description = "현재 로그인한 관리자의 기업 로고를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로고 업데이트 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PatchMapping("/company/logo")
    public BaseResponse<String> updateCompanyLogo(
            @Parameter(description = "로고 URL", required = true, example = "https://example.com/logo.png")
            @RequestParam @NotBlank(message = "로고 URL은 필수입니다") String logoUrl,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("기업 로고 업데이트 - userId: {}, logoUrl: {}", userId, logoUrl);
        adminService.updateCompanyLogo(userId, logoUrl);
        return BaseResponse.success("기업 로고가 성공적으로 변경되었습니다.");
    }

    // ========== 매니저 관리 (4개 API - ADMIN 권한 필요) ==========

    /**
     * 6. 매니저 목록 조회
     */
    @Operation(
            summary = "매니저 목록 조회",
            description = "현재 관리자의 기업에 소속된 매니저 목록을 조회합니다. (ADMIN 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/managers")
    public BaseResponse<ManagerDto.ManagerListResponse> getManagers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("매니저 목록 조회 - userId: {}, page: {}, size: {}", userId, page, size);

        // 권한 체크: ADMIN만 허용
        if (!"ADMIN".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        ManagerDto.ManagerListResponse response = adminService.getManagers(userId, page, size);
        return BaseResponse.success(response);
    }

    /**
     * 7. 매니저 계정 생성
     */
    @Operation(
            summary = "매니저 계정 생성",
            description = "관리자가 매니저 계정을 생성합니다. 생성된 계정 정보는 이메일로 발송됩니다. (ADMIN 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매니저 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "409", description = "이메일 중복")
            }
    )
    @PostMapping("/managers")
    public BaseResponse<ManagerDto.CreateResponse> createManager(
            @RequestBody @Valid ManagerDto.CreateRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("매니저 생성 - userId: {}, email: {}", userId, request.getEmail());

        // 권한 체크: ADMIN만 허용
        if (!"ADMIN".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        ManagerDto.CreateResponse response = adminService.createManager(request, userId);
        return BaseResponse.success(response);
    }

    /**
     * 8. 매니저 정보 수정
     */
    @Operation(
            summary = "매니저 정보 수정",
            description = "관리자가 매니저 정보를 수정합니다. (ADMIN 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매니저 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "매니저를 찾을 수 없음")
            }
    )
    @PatchMapping("/managers/{managerId}")
    public BaseResponse<ManagerDto.UpdateResponse> updateManager(
            @Parameter(description = "매니저 ID", required = true, example = "10")
            @PathVariable Long managerId,
            @RequestBody @Valid ManagerDto.UpdateRequest request,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("매니저 수정 - userId: {}, managerId: {}", userId, managerId);

        // 권한 체크: ADMIN만 허용
        if (!"ADMIN".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        ManagerDto.UpdateResponse response = adminService.updateManager(managerId, request, userId);
        return BaseResponse.success(response);
    }

    /**
     * 9. 매니저 계정 삭제
     */
    @Operation(
            summary = "매니저 계정 삭제",
            description = "관리자가 매니저 계정을 삭제합니다. (ADMIN 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매니저 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "매니저를 찾을 수 없음")
            }
    )
    @DeleteMapping("/managers/{managerId}")
    public BaseResponse<ManagerDto.ManagerDeleteResponse> deleteManager(
            @Parameter(description = "매니저 ID", required = true, example = "10")
            @PathVariable Long managerId,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("매니저 삭제 - userId: {}, managerId: {}", userId, managerId);

        // 권한 체크: ADMIN만 허용
        if (!"ADMIN".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        ManagerDto.ManagerDeleteResponse response = adminService.deleteManager(managerId, userId);
        return BaseResponse.success(response);
    }

    /**
     * 10. 이메일 중복 확인
     */
    @Operation(
            summary = "이메일 중복 확인",
            description = "관리자/매니저 이메일이 이미 사용 중인지 확인합니다. true: 사용 중, false: 사용 가능",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/check-email")
    public BaseResponse<Boolean> checkEmail(
            @Parameter(description = "이메일", required = true, example = "admin@example.com")
            @RequestParam @Email(message = "올바른 이메일 형식이 아닙니다") String email) {

        log.info("이메일 중복 확인 - email: {}", email);
        // TODO: UserService에 existsByEmailForAdmin 메서드 구현 필요
        // 현재는 임시로 false 반환
        return BaseResponse.success(false);
    }

    /**
     * 12. MANAGER → ADMIN 승격 (SUPER 전용)
     */
    @Operation(
            summary = "MANAGER를 ADMIN으로 승격",
            description = "SUPER가 MANAGER를 ADMIN으로 승격시킵니다. 기존 ADMIN은 MANAGER로 강등됩니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승격 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (MANAGER가 아님)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "SUPER 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 기업을 찾을 수 없음")
            }
    )
    @PostMapping("/managers/{managerId}/promote")
    public BaseResponse<AdminDto.PromoteResponse> promoteManagerToAdmin(
            @Parameter(description = "승격할 MANAGER ID", required = true, example = "15")
            @PathVariable Long managerId,
            @Parameter(description = "SUPER 사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long superUserId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("MANAGER ADMIN 승격 - managerId: {}, superUserId: {}", managerId, superUserId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        AdminDto.PromoteResponse response = adminService.promoteManagerToAdmin(managerId, superUserId);
        return BaseResponse.success(response);
    }
}