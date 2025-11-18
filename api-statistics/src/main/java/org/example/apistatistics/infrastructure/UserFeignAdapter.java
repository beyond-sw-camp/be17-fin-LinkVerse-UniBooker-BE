package org.example.apistatistics.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.common.base.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="userClient", url = "http://localhost:8085")
public interface UserFeignAdapter {

    /**
     * 관리자 대시보드 - 기업별 사용자 수 조회
     */
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_USER_COUNT_API")
    @GetMapping("/api/companies/user-count/{companyId}")  // ✅ companies로 수정
    BaseResponse<Integer> getAdminTotalDashboardUserCount(@PathVariable("companyId") Long companyId);


    /**
     * 슈퍼 관리자 대시보드 - 연도별 통계 조회
     */
    @CircuitBreaker(name = "GET_SUPER_DASHBOARD_API")
    @GetMapping("/api/companies/statistics/{year}")
    DashboardDto.YearlyStatisticsResponse getYearlyStatistics(@PathVariable("year") int year);
}