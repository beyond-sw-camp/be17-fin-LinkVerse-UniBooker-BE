package org.example.apistatistics.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.apistatistics.usecase.port.in.DashboardWebPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class DashboardUseCase implements DashboardWebPort {

    private final DashboardService dashboardService;
    private final DashboardFeignAdapter dashboardFeignAdapter;

    @Override
    public DashboardDto.AdminDashboardResponse getCompanyDashboard(Long companyId) {
        // 해당 회사(companyId)에 속한 리소스 그룹 목록 조회
        List<ResourceGroups> resourceGroups = resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);

        //  각 리소스 그룹별 예약 수, 리소스 수, 조회 수 집계
        int totalReservations = reservationRepository.countByCompanyId(companyId); // 회사 전체 예약
        int activeServiceGroups = resourceGroups.size(); // 활성화된 그룹 수
        int activeServices = resourceRepository.countActiveResourcesByCompanyId(companyId); // 활성 리소스 총합
        int userCount = userRepository.findAllByCompany_IdAndRole(companyId, UserRole.USER).size(); // 해당 회사에 소속된 사용자 수


        // Summary 데이터 구성
        DashboardDto.Summary summary = DashboardDto.Summary.builder()
                .totalReservations(totalReservations)
                .activeServiceGroups(activeServiceGroups)
                .activeServices(activeServices)
                .userCount(userCount)
                .build();

        // ServiceGroups 리스트 구성
        //    - 각 그룹별 이름, 서비스 수, 예약 수, 상태, 조회 수
        List<DashboardDto.ResourceGroupStats> groupStats = resourceGroups.stream()
                .map(resourceGroup -> {
                    int reservationCount = reservationRepository.countByResourceGroupId(resourceGroup.getId());
                    int serviceCount = resourceRepository.countByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(resourceGroup.getId());
                    String status = resourceGroup.getIsActive() ? "ACTIVE" : "INACTIVE";
                    int viewCount = resourceGroup.getViewCount();

                    return DashboardDto.ResourceGroupStats.builder()
                            .name(resourceGroup.getName())
                            .serviceCount(serviceCount)
                            .reservationCount(reservationCount)
                            .status(status)
                            .viewCount(viewCount)
                            .build();
                })
                .toList();

        // ReservationTrends 데이터 구성 (예: 최근 31일)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        // 실제 예약 데이터 조회
        List<Object[]> trendRaw = reservationRepository.countReservationsByGroupAndDate(companyId, oneMonthAgo, now);

        // 회사 소속 모든 그룹 조회
        List<String> groupNames = resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId)
                .stream().map(rg -> rg.getName()).toList();

        // 날짜별 트렌드 Map 초기화 (최근 한 달)
        Map<LocalDate, Map<String, Integer>> trendMap = new TreeMap<>();
        for (LocalDate date = oneMonthAgo.toLocalDate(); !date.isAfter(now.toLocalDate()); date = date.plusDays(1)) {
            Map<String, Integer> groups = new HashMap<>();
            for (String groupName : groupNames) {
                groups.put(groupName, 0); // 기본값 0
            }
            trendMap.put(date, groups);
        }

        // 실제 예약 데이터로 덮어쓰기
        for (Object[] row : trendRaw) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            String groupName = (String) row[1];
            int count = ((Number) row[2]).intValue();
            trendMap.get(date).put(groupName, count);
        }

        // DTO 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<DashboardDto.ReservationTrend> reservationTrends = trendMap.entrySet().stream()
                .map(e -> DashboardDto.ReservationTrend.builder()
                        .date(e.getKey().format(formatter))
                        .groups(e.getValue())
                        .build())
                .toList();


        // DashboardResponse DTO 빌드 및 반환
        return DashboardDto.AdminDashboardResponse.builder()
                .summary(summary)
                .serviceGroups(groupStats)
                .reservationTrends(reservationTrends)
                .build();
    }
}
