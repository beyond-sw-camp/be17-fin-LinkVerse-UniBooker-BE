package org.example.apistatistics.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.Digits;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name="reservationClient", url = "http://localhost:8082")
public interface ReservationFeignAdapter {

    // 관리자 전체 대시보드에 필요한 회사의 총 예약 수
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_RESERVATION_COUNT_API")
    @GetMapping("/api/reservation/company-counts/{companyId}")
    int getAdminTotalDashboardReservationCount(@PathVariable("companyId") Long companyId);


    // 관리자 전체 대시보드에 필요한 최근 한 달 간 리소스 그룹별 예약 수
    @CircuitBreaker(name = "GET_ADMIN_TOTAL_DASHBOARD_RESERVATION_TRENDS_API")
    @PostMapping("/api/reservation/trends")
    List<DashboardDto.DashboardReservationTrendResponse> getAdminReservationTrends(
            @RequestBody DashboardDto.ReservationTrendRequest request
    );


    // 관리자 전체 대시보드에 필요한 리소스 그룹별 예약 수
    @CircuitBreaker(name = "GET_ADMIN_DASHBOARD_RESERVATION_COUNT_BY_GROUP_API")
    @PostMapping("/api/reservation/group-counts")
    List<DashboardDto.GroupReservationCountResponse> getReservationCountsByGroupResources(
            @RequestBody List<Long> groupIds
    );
}
