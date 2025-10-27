package org.example.unibooker.domain.analytics.controller;

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

    @Operation(summary = "대시보드 요약")
    @GetMapping("/admin")
    public BaseResponse<DashboardDto.DashboardResponse> getCompanyDashboard(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser){
        DashboardDto.DashboardResponse response = dashboardService.getCompanyDashboard(authUser.getCompanyId());
        return BaseResponse.success(response);
    }

}
