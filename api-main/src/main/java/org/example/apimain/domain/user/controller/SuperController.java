package org.example.apimain.domain.user.controller;

import org.example.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.example.apimain.domain.user.model.dto.AuthDto;
import org.example.apimain.domain.user.model.dto.SuperDto;
import org.example.apimain.domain.user.model.dto.AdminDto;
import org.example.apimain.domain.company.model.dto.CompanyDto;
import org.example.apimain.domain.user.model.dto.UserDto;
import org.example.common.exception.BaseException;
import org.example.common.base.BaseResponseStatus;
import org.example.common.base.BaseResponse;
import org.example.apimain.domain.company.service.CompanyService;
import org.example.apimain.domain.user.service.AdminService;
import org.example.apimain.domain.user.service.AuthService;
import org.example.apimain.domain.user.service.SuperService;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 슈퍼 관리자 컨트롤러
 * - 슈퍼 관리자 로그인
 * - 기업 신청 승인/거절 관리
 * - 기업 및 관리자 관리
 */
@Slf4j
@Tag(name = "Super Admin API", description = "슈퍼 관리자 API")
@RestController
@RequestMapping("/api/super")
@RequiredArgsConstructor
public class SuperController {

    private final SuperService superService;
    private final AdminService adminService;
    private final CompanyService companyService;
    private final AuthService authService;

    // ========== 슈퍼 관리자 로그인 ==========

    /**
     * 1. 슈퍼 관리자 로그인
     */
    @Operation(
            summary = "슈퍼 관리자 로그인",
            description = "슈퍼 관리자 계정으로 로그인합니다. JWT 토큰을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping("/login")
    public BaseResponse<UserDto.LoginResponse> login(
            @RequestBody @Valid SuperDto.SuperLoginRequest request,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("슈퍼 관리자 로그인 시도 - email: {}", request.getEmail());

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
     * 1-1. 슈퍼 관리자 로그아웃
     */
    @Operation(
            summary = "슈퍼 관리자 로그아웃",
            description = "슈퍼 관리자 로그아웃을 처리합니다. Refresh Token을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping("/logout")
    public BaseResponse<AuthDto.LogoutResponse> logout(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String userRole,
            HttpServletResponse response) {  // ← HttpServletResponse 추가

        log.info("슈퍼 관리자 로그아웃 - userId: {}, role: {}", userId, userRole);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(userRole)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // 1. Refresh Token 삭제 (DB 또는 Redis)
        AuthDto.LogoutResponse logoutResponse = authService.logout(userId);

        // 2. 쿠키 삭제
        UserRole role = UserRole.valueOf(userRole);
        CookieUtil.deleteAllTokenCookies(response, role);

        return BaseResponse.success(logoutResponse);
    }

    // ========== 신청 관리 (4개 API) ==========

    /**
     * 2. 승인 대기 기업 목록 조회
     */
    @Operation(
            summary = "승인 대기 기업 목록 조회",
            description = "승인 대기 중인 기업 신청 목록을 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/applications")
    public BaseResponse<List<CompanyDto.PendingResponse>> getPendingApplications(
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("승인 대기 기업 목록 조회");

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        List<CompanyDto.PendingResponse> response = adminService.getPendingCompanies();
        return BaseResponse.success(response);
    }

    /**
     * 3. 기업 신청 상세 조회
     */
    @Operation(
            summary = "기업 신청 상세 조회",
            description = "특정 기업 신청의 상세 정보를 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @GetMapping("/applications/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getApplicationDetail(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 신청 상세 조회 - companyId: {}", companyId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 4. 기업 신청 승인
     */
    @Operation(
            summary = "기업 신청 승인",
            description = "기업 가입 신청을 승인합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "승인 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @PostMapping("/applications/{companyId}/approve")
    public BaseResponse<CompanyDto.ApprovalResponse> approveApplication(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 신청 승인 - companyId: {}, approvedBy: {}", companyId, userId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        CompanyDto.ApprovalResponse response = adminService.approveCompany(companyId, userId);
        return BaseResponse.success(response);
    }

    /**
     * 5. 기업 신청 거절
     */
    @Operation(
            summary = "기업 신청 거절",
            description = "기업 가입 신청을 거절합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "거절 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @PostMapping("/applications/{companyId}/reject")
    public BaseResponse<CompanyDto.ApprovalResponse> rejectApplication(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyDto.ApprovalRequest request,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 신청 거절 - companyId: {}", companyId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        CompanyDto.ApprovalResponse response =
                adminService.rejectCompany(companyId, request.getRejectionReason());
        return BaseResponse.success(response);
    }

    // ========== 기업 관리 (3개 API) ==========

    /**
     * 6. 기업 상세 조회
     */
    @Operation(
            summary = "기업 상세 조회",
            description = "특정 기업의 상세 정보를 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @GetMapping("/companies/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getCompanyDetail(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 상세 조회 - companyId: {}", companyId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 7. 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     */
    @Operation(
            summary = "기업 상태 변경",
            description = "기업의 서비스 상태를 변경합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @PatchMapping("/companies/{companyId}/status")
    public BaseResponse<CompanyDto.StatusUpdateResponse> updateCompanyStatus(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyDto.StatusUpdateRequest request,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 상태 변경 - companyId: {}, newStatus: {}", companyId, request.getStatus());

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        CompanyDto.StatusUpdateResponse response =
                companyService.updateCompanyStatus(companyId, request.getStatus());
        return BaseResponse.success(response);
    }

    /**
     * 8. 특정 기업의 관리자 목록 조회
     */
    @Operation(
            summary = "기업 관리자 목록 조회",
            description = "특정 기업의 관리자(ADMIN, MANAGER) 목록을 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @GetMapping("/companies/{companyId}/managers")
    public BaseResponse<SuperDto.CompanyManagerListResponse> getCompanyManagers(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("기업 관리자 목록 조회 - companyId: {}", companyId);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        SuperDto.CompanyManagerListResponse response = superService.getCompanyManagers(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 5-1. 전체 기업 목록 조회 (페이징)
     */
    @Operation(
            summary = "전체 기업 목록 조회",
            description = "활성 및 정지 상태의 모든 기업 목록을 페이징하여 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/companies")
    public BaseResponse<CompanyDto.CompanyListResponse> getAllCompanies(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "상태 필터 (ACTIVE, SUSPENDED)", example = "ACTIVE")
            @RequestParam(required = false) String statusFilter,
            @Parameter(description = "검색 키워드 (기업명)", example = "삼성")
            @RequestParam(required = false) String keyword,  // ← 추가
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("전체 기업 목록 조회 - page: {}, size: {}, statusFilter: {}, keyword: {}",
                page, size, statusFilter, keyword);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // String → CompanyStatus Enum 변환
        org.example.common.model.CompanyStatus companyStatus = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                companyStatus = org.example.common.model.CompanyStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_COMPANY_STATUS);
            }
        }

        // 4개 파라미터 모두 전달
        CompanyDto.CompanyListResponse response =
                companyService.getAllCompanies(page, size, companyStatus, keyword);

        return BaseResponse.success(response);
    }

    // ========== 관리자 관리 (2개 API) ==========

    /**
     * 9. 관리자+매니저 목록 조회
     */
    @Operation(
            summary = "관리자+매니저 목록 조회",
            description = "전체 관리자와 매니저 목록을 조회합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/managers")
    public BaseResponse<AdminDto.AdminListResponse> getAllManagers(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "역할 필터 (ADMIN, MANAGER)", example = "ADMIN")
            @RequestParam(required = false) String roleFilter,
            @Parameter(description = "상태 필터 (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
            @RequestParam(required = false) String statusFilter,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("관리자+매니저 목록 조회 - page: {}, size: {}", page, size);

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // String → Enum 변환
        UserRole userRole = null;
        if (roleFilter != null && !roleFilter.isBlank()) {
            try {
                userRole = UserRole.valueOf(roleFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_USER_ROLE);
            }
        }

        UserStatus userStatus = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                userStatus = UserStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BaseException(BaseResponseStatus.INVALID_USER_STATUS);
            }
        }

        AdminDto.AdminListResponse response =
                adminService.getAllAdmins(page, size, userRole, userStatus);
        return BaseResponse.success(response);
    }

    /**
     * 10. 관리자/매니저 상태 변경
     */
    @Operation(
            summary = "관리자/매니저 상태 변경",
            description = "관리자 또는 매니저의 계정 상태를 변경합니다. (SUPER 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    @PatchMapping("/managers/{userId}/status")
    public BaseResponse<String> updateManagerStatus(
            @Parameter(description = "사용자 ID", required = true, example = "10")
            @PathVariable Long userId,
            @RequestBody @Valid AdminDto.AdminStatusUpdateRequest request,
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") String role) {

        log.info("관리자/매니저 상태 변경 - userId: {}, newStatus: {}", userId, request.getStatus());

        // 권한 체크: SUPER만 허용
        if (!"SUPER".equals(role)) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        adminService.updateAdminStatus(userId, request);
        return BaseResponse.success("계정 상태가 성공적으로 변경되었습니다.");
    }
}