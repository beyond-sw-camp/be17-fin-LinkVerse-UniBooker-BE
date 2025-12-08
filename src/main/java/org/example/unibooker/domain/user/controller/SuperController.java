package org.example.unibooker.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.service.CompanyService;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.AdminDto;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.dto.SuperDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.service.AdminService;
import org.example.unibooker.domain.user.service.AuthService;
import org.example.unibooker.domain.user.service.SuperService;
import org.example.unibooker.utils.CookieUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final AuthService authService;
    private final AdminService adminService;
    private final CompanyService companyService;

    // ========== 슈퍼 관리자 로그인/로그아웃 ==========

    /**
     * 슈퍼 관리자 로그인
     * - 단일 세션 정책: 기존 모든 역할의 쿠키 삭제 후 새 쿠키 생성
     * - Access Token과 Refresh Token을 모두 HttpOnly Cookie에 저장
     */
    @Operation(summary = "슈퍼 관리자 로그인",
            description = "슈퍼 관리자 계정으로 로그인합니다. JWT 토큰이 HttpOnly 쿠키로 설정됩니다.")
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
    @Operation(summary = "슈퍼 관리자 로그아웃",
            description = "현재 로그인 세션을 종료하고 토큰을 삭제합니다.")
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin,  // 타입 변경: Long → AuthDto.AuthAdmin
            HttpServletResponse response) {

        // Refresh Token 삭제
        AuthDto.LogoutResponse logoutResponse = authService.logout(authAdmin.getId());  // authAdmin.getId() 사용

        CookieUtil.deleteAllTokenCookies(response, authAdmin.getRole());  // 권한 파라미터 추가

        return BaseResponse.success(logoutResponse);
    }

    // ========== 신청 관리 ==========

    /**
     * 승인 대기 기업 목록 조회
     */
    @Operation(summary = "승인 대기 기업 목록 조회",
            description = "승인 대기 중인 기업 신청 목록을 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/applications")
    public BaseResponse<List<CompanyDto.PendingResponse>> getPendingApplications() {
        List<CompanyDto.PendingResponse> response = adminService.getPendingCompanies();
        return BaseResponse.success(response);
    }

    /**
     * 신청 상세 조회
     */
    @Operation(summary = "기업 신청 상세 조회",
            description = "특정 기업 신청의 상세 정보를 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/applications/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getApplicationDetail(
            @PathVariable Long companyId) {

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 기업 승인
     */
    @Operation(summary = "기업 신청 승인",
            description = "기업 가입 신청을 승인합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/applications/{companyId}/approve")
    public BaseResponse<CompanyDto.ApprovalResponse> approveApplication(
            @PathVariable Long companyId,
            @AuthenticationPrincipal AuthDto.AuthAdmin authAdmin) {

        CompanyDto.ApprovalResponse response =
                adminService.approveCompany(companyId, authAdmin.getId());
        return BaseResponse.success(response);
    }

    /**
     * 기업 거절
     */
    @Operation(summary = "기업 신청 거절",
            description = "기업 가입 신청을 거절합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/applications/{companyId}/reject")
    public BaseResponse<CompanyDto.ApprovalResponse> rejectApplication(
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyDto.ApprovalRequest request) {

        CompanyDto.ApprovalResponse response =
                adminService.rejectCompany(companyId, request.getRejectionReason());
        return BaseResponse.success(response);
    }

    // ========== 기업 관리 ==========

    /**
     * 전체 기업 목록 조회 (페이징 + 필터링)
     */
    @Operation(summary = "전체 기업 목록 조회",
            description = "플랫폼의 전체 기업 목록을 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/companies")
    public BaseResponse<CompanyDto.CompanyListResponse> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        CompanyStatus companyStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                companyStatus = CompanyStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_COMPANY_STATUS);
            }
        }

        CompanyDto.CompanyListResponse response =
                companyService.getAllCompanies(page, size, companyStatus, keyword);

        return BaseResponse.success(response);
    }

    /**
     * 기업 상세 조회 (관리용)
     */
    @Operation(summary = "기업 상세 조회",
            description = "특정 기업의 상세 정보를 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/companies/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getCompanyDetail(
            @PathVariable Long companyId) {

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     */
    @Operation(summary = "기업 상태 변경",
            description = "기업의 서비스 상태를 변경합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/companies/{companyId}/status")
    public BaseResponse<CompanyDto.StatusUpdateResponse> updateCompanyStatus(
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyDto.StatusUpdateRequest request) {

        CompanyDto.StatusUpdateResponse response =
                companyService.updateCompanyStatus(companyId, request.getStatus());

        return BaseResponse.success(response);
    }

    /**
     * 특정 기업의 관리자 목록 조회
     */
    @Operation(summary = "기업 관리자 목록 조회",
            description = "특정 기업의 관리자(ADMIN, MANAGER) 목록을 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/companies/{companyId}/managers")
    public BaseResponse<SuperDto.CompanyManagerListResponse> getCompanyManagers(
            @PathVariable Long companyId) {

        SuperDto.CompanyManagerListResponse response =
                superService.getCompanyManagers(companyId);
        return BaseResponse.success(response);
    }

    // ========== 관리자 관리 ==========

    /**
     * 관리자+매니저 목록 조회
     */
    @Operation(summary = "관리자+매니저 목록 조회",
            description = "전체 관리자와 매니저 목록을 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/managers")
    public BaseResponse<AdminDto.AdminListResponse> getAllManagers(
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

        AdminDto.AdminListResponse response =
                adminService.getAllAdmins(page, size, userRole, userStatus);
        return BaseResponse.success(response);
    }

    /**
     * 관리자/매니저 상태 변경
     */
    @Operation(summary = "관리자/매니저 상태 변경",
            description = "관리자 또는 매니저의 계정 상태를 변경합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/managers/{userId}/status")
    public BaseResponse<String> updateManagerStatus(
            @PathVariable Long userId,
            @RequestBody @Valid AdminDto.AdminStatusUpdateRequest request) {

        adminService.updateAdminStatus(userId, request);
        return BaseResponse.success("계정 상태가 성공적으로 변경되었습니다.");
    }
}