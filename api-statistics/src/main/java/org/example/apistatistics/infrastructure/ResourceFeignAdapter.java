package org.example.apistatistics.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="resourceClient", url = "http://localhost:8081")
public interface ResourceFeignAdapter {

    // 관리자 전체 대시보드에 필요한 리소스 그룹 데이터 조회
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_INFO_API")
    @GetMapping("/api/resource-group/total-dashboard/{companyId}")
    DashboardDto.AdminDashboardResourceGroup getAdminTotalDashboardInfo(@PathVariable("companyId") Long companyId);

    // 플랫폼 관리자 전체 대시보드에 필요한 리소스 그룹 데이터 조회
    @CircuitBreaker(name = "GET_SUPER_TOTAL_DASHBOARD_INFO_API")
    @GetMapping("/api/resource-group/total-dashboard")
    DashboardDto.ServiceStatsResponse getServiceStatistics();

    // ✅ 추가: 리소스 그룹별 대시보드 데이터 조회
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_DASHBOARD_API")
    @GetMapping("/api/resource-group/group-dashboard/{resourceGroupId}")
    DashboardDto.ResourceGroupDashboardResponse getResourceGroupDashboard(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );
}