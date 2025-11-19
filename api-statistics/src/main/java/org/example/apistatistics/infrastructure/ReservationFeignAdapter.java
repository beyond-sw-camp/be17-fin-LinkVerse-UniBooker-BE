package org.example.apistatistics.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.apistatistics.domain.model.dto.ReservationTrendCommand;
import org.example.common.base.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "api-reservation")
public interface ReservationFeignAdapter {

    // 관리자 전체 대시보드에 필요한 회사의 총 예약 수
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_RESERVATION_COUNT_API")
    @GetMapping("/api/reservation/company-counts/{companyId}")
    Integer getAdminTotalDashboardReservationCount(
            @PathVariable("companyId") Long companyId
    );

    // 최근 한 달 리소스 그룹별 예약 추이
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_RESERVATION_TRENDS_API")
    @PostMapping("/api/reservation/trends")
    List<DashboardDto.DashboardReservationTrendResponse> getAdminReservationTrends(
            @RequestBody DashboardDto.ReservationTrendRequest request
    );

    // 리소스 그룹별 예약 수
    @CircuitBreaker(name = "GET_ADMIN_DASHBOARD_RESERVATION_COUNT_BY_GROUP_API")
    @PostMapping("/api/reservation/group-counts")
    List<DashboardDto.GroupReservationCountResponse> getReservationCountsByGroupResources(
            @RequestBody List<Long> groupIds);

    // 리소스 그룹의 누적 예약수
    @CircuitBreaker(name = "GET_CUM_RESERVATION_COUNT_BY_GROUP_API")
    @GetMapping("/api/reservation/cum-reservation/{resourceGroupId}")
    Integer getCumReservationCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );

    // 리소스 그룹의 누적 취소수
    @CircuitBreaker(name = "GET_CUM_CANCEL_COUNT_BY_GROUP_API")
    @GetMapping("/api/reservation/cum-cancel/{resourceGroupId}")
    Integer getCumCancelCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );

    // 리소스 그룹별 예약 수 (한 달치)
    @CircuitBreaker(name = "GET_PERFORMANCE_BY_RESOURCE_API")
    @GetMapping("/api/reservation/resource-performance/{resourceGroupId}")
    List<DashboardDto.ServicePerformanceCount> getServicePerformanceCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );

    // 리소스 그룹에 속하는 사용자 수
    @CircuitBreaker(name = "GET_USER_COUNT_BY_GROUP_API")
    @GetMapping("/api/reservation/visitor/{resourceGroupId}/{companyId}")
    DashboardDto.UserCountResponse getUserCount(
            @PathVariable("resourceGroupId") Long resourceGroupId,
            @PathVariable("companyId") Long companyId
    );

    // 성별
    @CircuitBreaker(name = "GET_USERS_GENDER_API")
    @GetMapping("/api/reservation/gender/{resourceGroupId}")
    List<DashboardDto.ReservationGenderInfo> getGenderCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );

    // 나이
    @CircuitBreaker(name = "GET_USERS_AGE_API")
    @GetMapping("/api/reservation/age/{resourceGroupId}")
    List<DashboardDto.ReservationAgeInfo> getAgeCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );

    // 시간대별 예약 수
    @CircuitBreaker(name = "GET_HOURLY_RESERVATION_API")
    @GetMapping("/api/reservation/time-slot/{resourceGroupId}")
    List<DashboardDto.TimeSlotReservationCount> getHourlyReservationCount(
            @PathVariable("resourceGroupId") Long resourceGroupId
    );
}