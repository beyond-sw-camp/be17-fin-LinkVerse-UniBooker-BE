package org.example.apistatistics.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="api-app")
public interface UserFeignAdapter {

    // 관리자 전체 대시보드에 필요한 회사 가입 고객 수
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_USER_COUNT_API")
    @GetMapping("/api/companies/user-count/{companyId}")
    int getAdminTotalDashboardUserCount(@PathVariable("companyId") Long companyId);


        @CircuitBreaker(name = "GET_SUPER_DASHBOARD_API")
        @GetMapping("/api/companies/statistics/{year}")
        DashboardDto.YearlyStatisticsResponse getYearlyStatistics(@PathVariable("year") int year);
}
