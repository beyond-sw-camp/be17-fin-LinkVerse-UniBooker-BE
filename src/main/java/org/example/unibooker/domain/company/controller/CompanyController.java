package org.example.unibooker.domain.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.company.service.CompanyService;
import org.example.unibooker.domain.user.model.dto.SuperDto;
import org.example.unibooker.domain.user.service.AdminService;
import org.example.unibooker.domain.user.service.SuperService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Company", description = "기업 관리 API (슈퍼 관리자 전용)")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final AdminService adminService;
    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final SuperService superService;

    @Operation(summary = "Company Slug 중복 확인",
            description = "회원가입 시 사용할 Company Slug의 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-slug")
    public BaseResponse<CompanyDto.SlugCheckResponse> checkSlug(
            @RequestParam String slug) {

        CompanyDto.SlugCheckResponse response = companyService.checkSlugAvailability(slug);
        return BaseResponse.success(response);
    }

    @Operation(summary = "사업자등록번호 중복 확인",
            description = "사업자등록번호가 이미 등록되어 있는지 확인합니다.")
    @GetMapping("/check-business-number")
    public BaseResponse<Boolean> checkBusinessNumber(
            @RequestParam @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$",
                    message = "올바른 사업자등록번호 형식이 아닙니다")
            String businessNumber) {

        boolean exists = companyRepository.existsByBusinessNumber(businessNumber);
        return BaseResponse.success(exists);
    }

    @Operation(summary = "승인 대기 기업 목록 조회", description = "승인 대기 중인 기업 목록을 조회합니다.")
    @GetMapping("/pending")
    public BaseResponse<List<CompanyDto.PendingResponse>> getPendingCompanies() {
        List<CompanyDto.PendingResponse> response = adminService.getPendingCompanies();
        return BaseResponse.success(response);
    }

    @Operation(summary = "기업 상세 조회", description = "특정 기업의 상세 정보를 조회합니다.")
    @GetMapping("/{companyId}")
    public BaseResponse<CompanyDto.DetailResponse> getCompanyDetail(
            @PathVariable Long companyId) {

        CompanyDto.DetailResponse response = adminService.getCompanyDetail(companyId);
        return BaseResponse.success(response);
    }

    @Operation(summary = "기업 승인", description = "기업 가입 신청을 승인합니다.")
    @PostMapping("/{companyId}/approve")
    public BaseResponse<CompanyDto.ApprovalResponse> approveCompany(
            @PathVariable Long companyId,
            @AuthenticationPrincipal Long approvedBy) {

        CompanyDto.ApprovalResponse response = adminService.approveCompany(companyId, approvedBy);
        return BaseResponse.success(response);
    }

    @Operation(summary = "기업 거절", description = "기업 가입 신청을 거절합니다.")
    @PostMapping("/{companyId}/reject")
    public BaseResponse<CompanyDto.ApprovalResponse> rejectCompany(
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyDto.ApprovalRequest request) {

        CompanyDto.ApprovalResponse response = adminService.rejectCompany(companyId, request.getRejectionReason());
        return BaseResponse.success(response);
    }

    /**
     * Company Slug로 기업 정보 조회 (일반 사용자용)
     */
    @Operation(summary = "Company Slug로 기업 정보 조회",
            description = "Company Slug를 통해 기업 정보를 조회합니다. (일반 사용자 회원가입용)")
    @GetMapping("/slug/{companySlug}")
    public BaseResponse<CompanyDto.PublicInfoResponse> getCompanyBySlug(
            @PathVariable @Schema(description = "Company Slug", example = "company-a") String companySlug) {

        CompanyDto.PublicInfoResponse response = companyService.getCompanyBySlug(companySlug);
        return BaseResponse.success(response);
    }

    /**
     * 전체 기업 목록 조회 (페이징 + 필터링)
     */
    @Operation(summary = "전체 기업 목록 조회",
            description = "플랫폼의 전체 기업 목록을 조회합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @GetMapping
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
                throw new BaseException(
                        BaseResponseStatus.INVALID_COMPANY_STATUS);
            }
        }

        CompanyDto.CompanyListResponse response =
                companyService.getAllCompanies(page, size, companyStatus, keyword);

        return BaseResponse.success(response);
    }

    /**
     * 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     */
    @Operation(summary = "기업 상태 변경",
            description = "기업의 서비스 상태를 변경합니다. (SUPER 권한 필요)")
    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/{companyId}/status")
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
    @GetMapping("/{companyId}/managers")
    public BaseResponse<SuperDto.CompanyManagerListResponse> getCompanyManagers(
            @PathVariable Long companyId) {

        SuperDto.CompanyManagerListResponse response = superService.getCompanyManagers(companyId);
        return BaseResponse.success(response);
    }
}