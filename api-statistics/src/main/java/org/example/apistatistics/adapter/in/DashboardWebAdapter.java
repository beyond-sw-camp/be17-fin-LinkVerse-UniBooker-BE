package org.example.apistatistics.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.apistatistics.usecase.port.in.DashboardWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 대시보드 데이터 조회 컨트롤러
 * - 관리자 대시보드 데이터 조회
 * - 플랫폼 관리자 대시보드 데이터 조회
 * - 리소스 그룹별 대시보드 데이터 조회
 */
@Slf4j
@Tag(name = "Dashboard API", description = "대시보드 데이터 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardWebAdapter {

    /** 대시보드 웹 포트 */
    private final DashboardWebPort dashboardWebPort;

    /**
     * 관리자 대시보드 데이터 조회
     */
    @Operation(
            summary = "관리자 대시보드 데이터 조회",
            description = "기업 관리자를 위한 전체 대시보드 데이터를 조회합니다. (요약 통계, 서비스 그룹별 현황, 예약 트렌드)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/admin")
    public BaseResponse<DashboardDto.AdminDashboardResponse> getCompanyDashboard(
            @Parameter(description = "기업 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-Company-Id") Long companyId) {

        log.info("관리자 대시보드 데이터 조회 - companyId: {}", companyId);
        DashboardDto.AdminDashboardResponse response = dashboardWebPort.getCompanyDashboard(companyId);
        return BaseResponse.success(response);
    }

    /**
     * 플랫폼 관리자 대시보드 데이터 조회
     */
    @Operation(
            summary = "플랫폼 관리자 대시보드 데이터 조회",
            description = "플랫폼 관리자를 위한 전체 대시보드 데이터를 조회합니다. (기업 통계, 고객 통계, 서비스 통계, 에러 로그)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (슈퍼 관리자만 가능)")
            }
    )
    @GetMapping("/super")
    public BaseResponse<DashboardDto.SuperDashboardResponse> getPlatformDashboard() {
        log.info("플랫폼 관리자 대시보드 데이터 조회");
        DashboardDto.SuperDashboardResponse response = dashboardWebPort.getPlatformDashboard();
        return BaseResponse.success(response);
    }

    /**
     * 리소스 그룹별 대시보드 데이터 조회
     */
    @Operation(
            summary = "리소스 그룹별 대시보드 데이터 조회",
            description = "특정 리소스 그룹의 상세 대시보드 데이터를 조회합니다. (리소스별 성과, 예약 통계, 사용자 특성, 시간대별 현황)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "리소스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/resource-group/{resourceGroupId}")
    public BaseResponse<DashboardDto.ResourceGroupDashboardData> getResourceGroupDashboard(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId,

            @Parameter(description = "기업 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-Company-Id") Long companyId) {

        log.info("리소스 그룹별 대시보드 데이터 조회 - resourceGroupId: {}, companyId: {}", resourceGroupId, companyId);
        DashboardDto.ResourceGroupDashboardData response =
                dashboardWebPort.getResourceGroupDashboard(resourceGroupId, companyId);
        return BaseResponse.success(response);
    }
}