package org.example.unibooker.domain.analytics.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.analytics.model.DashboardDto;
import org.example.unibooker.domain.analytics.service.DashboardService;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 데이터를 조회합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(summary = "관리자 대시보드")
    @GetMapping("/admin")
    public BaseResponse<DashboardDto.AdminDashboardResponse> getCompanyDashboard(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser){
        DashboardDto.AdminDashboardResponse response = dashboardService.getCompanyDashboard(authUser.getCompanyId());
        return BaseResponse.success(response);
    }

    @Operation(summary = "플랫폼 관리자 대시보드")
    @GetMapping("/super")
    public BaseResponse<DashboardDto.SuperDashboardResponse> getPlatformDashboard(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser){
        DashboardDto.SuperDashboardResponse response = dashboardService.getPlatformDashboard(authUser);
        return BaseResponse.success(response);
    }

}
