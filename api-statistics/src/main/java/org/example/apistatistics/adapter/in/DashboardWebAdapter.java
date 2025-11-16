package org.example.apistatistics.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.apistatistics.usecase.port.in.DashboardWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 데이터를 조회합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardWebAdapter {

    private final DashboardWebPort dashboardWebPort;

    @Operation(summary = "관리자 대시보드")
    @GetMapping("/admin")
    public BaseResponse<DashboardDto.AdminDashboardResponse> getCompanyDashboard(@RequestHeader("X-Company-Id") Long companyId){
        DashboardDto.AdminDashboardResponse response = dashboardWebPort.getCompanyDashboard(companyId);
        return BaseResponse.success(response);
    }


    @Operation(summary = "플랫폼 관리자 대시보드")
    @GetMapping("/super")
    public BaseResponse<DashboardDto.SuperDashboardResponse> getPlatformDashboard(){
        DashboardDto.SuperDashboardResponse response = dashboardWebPort.getPlatformDashboard();
        return BaseResponse.success(response);
    }
}
