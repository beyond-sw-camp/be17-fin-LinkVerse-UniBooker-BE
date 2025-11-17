package org.example.apistatistics.usecase.port.in;

import org.example.apistatistics.domain.model.dto.DashboardDto;

public interface DashboardWebPort {

    // 관리자 전체 대시보드 조회
    DashboardDto.AdminDashboardResponse getCompanyDashboard(Long companyId);

    // 플랫폼 관리자 대시보드 조회
    DashboardDto.SuperDashboardResponse getPlatformDashboard();

    // 관리자 리소스 그룹별 대시보드 조회
    DashboardDto.ResourceGroupDashboardData getResourceGroupDashboard(Long resourceGroupId, Long companyId);
}