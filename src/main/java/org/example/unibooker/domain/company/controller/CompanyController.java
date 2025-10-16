package org.example.unibooker.domain.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.company.service.CompanyService;
import org.example.unibooker.domain.user.service.AdminService;
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
}