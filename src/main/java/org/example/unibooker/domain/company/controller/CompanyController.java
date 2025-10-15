package org.example.unibooker.domain.company.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.company.model.CompanyDto;
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