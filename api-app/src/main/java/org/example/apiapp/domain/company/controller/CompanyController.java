package org.example.apiapp.domain.company.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.model.CompanyStatus;
import org.example.apiapp.domain.company.model.dto.CompanyDto;
import org.example.apiapp.domain.company.repository.CompanyRepository;
import org.example.apiapp.domain.company.service.CompanyService;
import org.example.apiapp.domain.user.model.dto.SuperDto;
import org.example.apiapp.domain.user.service.AdminService;
import org.example.apiapp.domain.user.service.SuperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기업 관리 컨트롤러
 * - Company Slug 검증 및 중복 확인
 * - 기업 정보 조회 (일반 사용자용)
 * - 기업 목록 조회 및 상태 관리 (슈퍼 관리자용)
 */
@Slf4j
@Tag(name = "Company API", description = "기업 관리 API")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    /** 관리자 서비스 */
    private final AdminService adminService;

    /** 기업 레포지토리 */
    private final CompanyRepository companyRepository;

    /** 기업 서비스 */
    private final CompanyService companyService;

    /** 슈퍼 관리자 서비스 */
    private final SuperService superService;

    // ========== 공개 API ==========

    /**
     * Company Slug 중복 확인
     */
    @Operation(
            summary = "Company Slug 중복 확인",
            description = "회원가입 시 사용할 Company Slug의 사용 가능 여부를 확인합니다."
    )
    @GetMapping("/check-slug")
    public BaseResponse<CompanyDto.SlugCheckResponse> checkSlug(
            @RequestParam @Schema(description = "확인할 Company Slug", example = "company-a")
            String slug) {

        CompanyDto.SlugCheckResponse response = companyService.checkSlugAvailability(slug);
        return BaseResponse.success(response);
    }

    /**
     * 사업자등록번호 중복 확인
     */
    @Operation(
            summary = "사업자등록번호 중복 확인",
            description = "사업자등록번호가 이미 등록되어 있는지 확인합니다."
    )
    @GetMapping("/check-business-number")
    public BaseResponse<Boolean> checkBusinessNumber(
            @RequestParam
            @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$",
                    message = "올바른 사업자등록번호 형식이 아닙니다")
            @Schema(description = "사업자등록번호", example = "123-45-67890")
            String businessNumber) {

        boolean exists = companyRepository.existsByBusinessNumber(businessNumber);
        return BaseResponse.success(exists);
    }

    /**
     * Company Slug로 기업 정보 조회 (일반 사용자용)
     */
    @Operation(
            summary = "Company Slug로 기업 정보 조회",
            description = "Company Slug를 통해 기업 정보를 조회합니다. (일반 사용자 회원가입용)"
    )
    @GetMapping("/slug/{companySlug}")
    public BaseResponse<CompanyDto.PublicInfoResponse> getCompanyBySlug(
            @PathVariable
            @Schema(description = "Company Slug", example = "company-a")
            String companySlug) {

        CompanyDto.PublicInfoResponse response = companyService.getCompanyBySlug(companySlug);
        return BaseResponse.success(response);
    }

    // ========== SUPER 권한 API ==========

    /**
     * 기업 상세 조회
     */
    @Operation(
            summary = "기업 상세 조회",
            description = "특정 기업의 상세 정보를 조회합니다."
    )
    @GetMapping("/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getCompanyDetail(
            @PathVariable
            @Schema(description = "기업 ID", example = "1")
            Long companyId) {

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 전체 기업 목록 조회 (페이징 + 필터링)
     */
    @Operation(
            summary = "전체 기업 목록 조회",
            description = "플랫폼의 전체 기업 목록을 조회합니다. (SUPER 권한 필요)"
    )
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping
    public BaseResponse<CompanyDto.CompanyListResponse> getAllCompanies(
            @RequestParam(defaultValue = "0")
            @Schema(description = "페이지 번호", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Schema(description = "페이지 크기", example = "10")
            int size,

            @RequestParam(required = false)
            @Schema(description = "기업 상태 필터", example = "ACTIVE")
            String status,

            @RequestParam(required = false)
            @Schema(description = "검색 키워드", example = "ABC")
            String keyword) {

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
     * 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     */
    @Operation(
            summary = "기업 상태 변경",
            description = "기업의 서비스 상태를 변경합니다. (SUPER 권한 필요)"
    )
    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/{companyId}/status")
    public BaseResponse<CompanyDto.StatusUpdateResponse> updateCompanyStatus(
            @PathVariable
            @Schema(description = "기업 ID", example = "1")
            Long companyId,

            @RequestBody @Valid CompanyDto.StatusUpdateRequest request) {

        CompanyDto.StatusUpdateResponse response =
                companyService.updateCompanyStatus(companyId, request.getStatus());

        return BaseResponse.success(response);
    }

    /**
     * 특정 기업의 관리자 목록 조회
     */
    @Operation(
            summary = "기업 관리자 목록 조회",
            description = "특정 기업의 관리자(ADMIN, MANAGER) 목록을 조회합니다. (SUPER 권한 필요)"
    )
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/{companyId}/managers")
    public BaseResponse<SuperDto.CompanyManagerListResponse> getCompanyManagers(
            @PathVariable
            @Schema(description = "기업 ID", example = "1")
            Long companyId) {

        SuperDto.CompanyManagerListResponse response = superService.getCompanyManagers(companyId);
        return BaseResponse.success(response);
    }

    @Operation(summary = "플랫폼 대시보드 데이터 조회", description = "기간별 사용자(기업, 고객) 가입 수, 현재 활성화된 사용자(기업, 고객) 수를 조회합니다.")
    @GetMapping("/statistics/{year}")
    public CompanyDto.YearlyStatisticsResponse getStatistics(@PathVariable int year) {
        return companyService.getStatisticsByYear(year);
    }

    // ========== 내부 API (Gateway 전용) ==========

    /**
     * [내부 API] Company Slug로 상태 조회 (Gateway 전용)
     */
    @Operation(
            summary = "[내부] Company Slug로 상태 조회",
            description = "Gateway에서 Company 상태를 확인하기 위한 내부 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "기업을 찾을 수 없음")
            }
    )
    @GetMapping("/internal/slug/{companySlug}/status")
    public BaseResponse<CompanyDto.StatusOnlyResponse> getCompanyStatusBySlug(
            @PathVariable
            @Schema(description = "Company Slug", required = true, example = "test-company")
            String companySlug) {

        log.info("[내부 API] Company 상태 조회 - companySlug: {}", companySlug);

        CompanyDto.StatusOnlyResponse response = companyService.getCompanyStatusBySlug(companySlug);
        return BaseResponse.success(response);
    }



    /**
     * 관리자 전체 대시보드에 필요한 회사 가입 고객 수 조회
     */
    @Operation(summary = "관리자 전체 대시보드 데이터 조회", description = "기업의 전체 고객수를 반환합니다.")
    @GetMapping("/user-count/{companyId}")
    public int getAdminTotalDashboardUserCount(@PathVariable Long companyId) {
        return companyService.getUserCountByCompanyId(companyId);
    }
}