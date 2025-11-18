package org.example.apistatistics.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apistatistics.domain.model.dto.DashboardDto;
import org.example.apistatistics.domain.model.dto.ReservationTrendCommand;
import org.example.apistatistics.domain.service.DashboardService;
import org.example.apistatistics.infrastructure.ReservationFeignAdapter;
import org.example.apistatistics.infrastructure.ResourceFeignAdapter;
import org.example.apistatistics.infrastructure.UserFeignAdapter;
import org.example.apistatistics.usecase.port.in.DashboardWebPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardUseCase implements DashboardWebPort {

    private final ResourceFeignAdapter resourceFeignAdapter;
    private final UserFeignAdapter userFeignAdapter;
    private final ReservationFeignAdapter reservationFeignAdapter;


    // 관리자 전체 대시보드
    @Override
    @Transactional
    public DashboardDto.AdminDashboardResponse getCompanyDashboard(Long companyId) {

        // 해당 회사(companyId)에 속한 리소스 그룹(이름, 아이디, 조회수, 활성화 상태) 정보와 총 그룹 수, 총 리소스 수
        DashboardDto.AdminDashboardResourceGroup resourceGroups = resourceFeignAdapter.getAdminTotalDashboardInfo(companyId);

        // 해당 회사의 총 고객 수
        int userCount = userFeignAdapter.getAdminTotalDashboardUserCount(companyId).getData();

        // 해당 회사의 총 예약 수
        int reservationCount = reservationFeignAdapter.getAdminTotalDashboardReservationCount(companyId).getData();


        // Summary 데이터 구성
        DashboardDto.AdminDashboardSummary summary = DashboardDto.AdminDashboardSummary.builder()
                .totalReservations(reservationCount)
                .activeServiceGroups(resourceGroups.getGroupCount())
                .activeServices(resourceGroups.getResourceCount())
                .userCount(userCount)
                .build();


        List<Long> groupIds = resourceGroups.getGroups().stream()
                .map(DashboardDto.AdminDashboardGroupInfo::getId)
                .toList();

        // 그룹별 예약 수 한 번에 조회
        // 그룹별 예약 수 한 번에 조회
// ✅ 1단계: Command 객체 생성
        ReservationTrendCommand command = ReservationTrendCommand.builder()
                .groupIds(groupIds)                           // 리소스 그룹 ID 리스트
                .from(LocalDateTime.now().minusMonths(1))     // 최근 1개월 전
                .to(LocalDateTime.now())                      // 현재
                .build();

// ✅ 2단계: Command 객체로 호출
        List<DashboardDto.GroupReservationCountResponse> reservationCounts =
                reservationFeignAdapter.getReservationCountsByGroupResources(command).getData();

        // Map으로 변환
        Map<Long, Integer> reservationMap = reservationCounts.stream()
                .collect(Collectors.toMap(
                        DashboardDto.GroupReservationCountResponse::getGroupId,
                        DashboardDto.GroupReservationCountResponse::getCount
                ));


        // ServiceGroups 리스트 구성
        //    - 각 그룹별 이름, 서비스 수, 예약 수, 상태, 조회 수
        List<DashboardDto.ResourceGroupStats> groupStats = resourceGroups.getGroups().stream()
                .map(group -> {

                    // 1) 그룹 ID로 예약 수 조회 (없으면 기본값 0)
                    int reservationCountByGroup = reservationMap.getOrDefault(group.getId(), 0);

                    // 2 ) 상태 설정
                    String status = group.getIsActive() ? "ACTIVE" : "INACTIVE";

                    return DashboardDto.ResourceGroupStats.builder()
                            .name(group.getName())
                            .serviceCount(group.getServiceCount())
                            .reservationCount(reservationCountByGroup)
                            .status(status)
                            .viewCount(group.getViewCount())
                            .build();
                })
                .toList();

        // 최근 한 달간 예약 트렌드 조회
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);


        DashboardDto.ReservationTrendRequest request =
                DashboardDto.ReservationTrendRequest.builder()
                        .groupIds(groupIds)      // 리소스 그룹 ID 리스트
                        .from(oneMonthAgo)
                        .to(now)
                        .build();


        // 실제 예약 데이터 조회 (예약 API에서 (날짜, 그룹아이디, 예약수) 형식으로 조회)
        List<DashboardDto.DashboardReservationTrendResponse> trendRaw =
                reservationFeignAdapter.getAdminReservationTrends(request).getData();

        // groupId → groupName 매핑
        Map<Long, String> groupIdNameMap = resourceGroups.getGroups().stream()
                .collect(Collectors.toMap(
                        DashboardDto.AdminDashboardGroupInfo::getId,
                        DashboardDto.AdminDashboardGroupInfo::getName
                ));

        // 날짜별 트렌드 Map 초기화 (최근 한 달)
        Map<LocalDate, Map<String, Integer>> trendMap = new TreeMap<>();

        List<String> groupNames = resourceGroups.getGroups().stream()
                .map(DashboardDto.AdminDashboardGroupInfo::getName)
                .toList();

        for (LocalDate date = oneMonthAgo.toLocalDate(); !date.isAfter(now.toLocalDate()); date = date.plusDays(1)) {
            Map<String, Integer> groups = new HashMap<>();
            for (String groupName : groupNames) {
                groups.put(groupName, 0); // 기본값 0
            }
            trendMap.put(date, groups);
        }

        // 실제 예약 데이터로 덮어쓰기 (groupId → groupName 변환)
        for (DashboardDto.DashboardReservationTrendResponse trend : trendRaw) {
            LocalDate date = trend.getDate();
            Long groupId = trend.getGroupId();
            int count = trend.getCount();

            // ID → 이름 변환
            String groupName = groupIdNameMap.get(groupId);
            if (groupName == null) continue;

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



    // 플랫폼 관리자 대시보드
    @Override
    @Transactional
    public DashboardDto.SuperDashboardResponse getPlatformDashboard() {


        // 오늘 날짜 기준으로 올해 설정
        int year = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(year, 1, 1);

        // 단일 요청으로 모든 연간 데이터 조회
        DashboardDto.YearlyStatisticsResponse stats = userFeignAdapter.getYearlyStatistics(year);

        // 기업 통계
        DashboardDto.CompanyStats companyStats = DashboardDto.CompanyStats.builder()
                .currentCompanyCount(stats.getTotalCompanies())
                .monthlyNewRegistrations(stats.getMonthlyNewCompanies())
                .build();

        // 고객 통계
        DashboardDto.CustomerStats customerStats = DashboardDto.CustomerStats.builder()
                .currentCustomerCount(stats.getTotalCustomers())
                .cumulativeRegistrations(stats.getMonthlyNewCustomers())
                .build();

        /** ------------------ 서비스 통계 ------------------ **/
        DashboardDto.ServiceStatsResponse resourceGroupStates  = resourceFeignAdapter.getServiceStatistics();

        DashboardDto.ServiceStats serviceStats = DashboardDto.ServiceStats.builder()
                .totalServiceCount(resourceGroupStates.getTotalServiceCount())
                .categoryCounts(resourceGroupStates.getCategoryCounts())
                .categoryLabels(resourceGroupStates.getCategoryLabels())
                .build();

        /** ------------------ 에러 로그 (임시) ------------------ **/
        List<DashboardDto.ErrorLogs> errorLogs = new ArrayList<>(); // 추후 로깅 서비스 연동 예정

        /** ------------------ 전체 응답 조립 ------------------ **/
        return DashboardDto.SuperDashboardResponse.builder()
                .companyStats(companyStats)
                .customerStats(customerStats)
                .serviceStats(serviceStats)
                .errorLogs(errorLogs)
                .build();
    }



    // 관리자 리소스 그룹별 대시보드
    @Override
    @Transactional
    public DashboardDto.ResourceGroupDashboardData getResourceGroupDashboard(Long resourceGroupId, Long companyId) {
        // 총 서비스 수
        // 서비스 그룹에 속하는 서비스의 예약 가능 수
        // 서비스 그룹의 조회수 (어제, 오늘, 시간대별)
        DashboardDto.ResourceGroupDashboardResponse resourceData = resourceFeignAdapter.getResourceGroupDashboard(resourceGroupId);

        // 리소스 그룹의 누적 예약수
        Integer cumReservationCount = reservationFeignAdapter.getCumReservationCount(resourceGroupId).getData();

        // 리소스 그룹의 누적 취소수
        Integer cumCancalCount = reservationFeignAdapter.getCumCancleCount(resourceGroupId).getData();

        // 리소스 그룹별 예약 수
        List<DashboardDto.ServicePerformanceCount> servicePerReservationCount =
                reservationFeignAdapter.getServicePerformanceCount(resourceGroupId).getData();
        // possibleTimeCount - reservedCount (리소스 별 성과 계산
        // (1) 예약 맵으로 변환
        Map<Long, Integer> reservedMap = servicePerReservationCount.stream()
                .collect(Collectors.toMap(
                        DashboardDto.ServicePerformanceCount::getResourceId,
                        DashboardDto.ServicePerformanceCount::getCount
                ));

        // (2) 리소스 정보 기반으로 PerformancePerResource 리스트 생성
        List<DashboardDto.PerformancePerResource> performancePerResources =
                resourceData.getResources().stream()
                        .map(info -> {
                            int reserved = reservedMap.getOrDefault(info.getResourceId(), 0);
                            int remaining = info.getPossibleTimeCount() - reserved;

                            return DashboardDto.PerformancePerResource.builder()
                                    .resourceName(info.getResourceName())
                                    .count(remaining)
                                    .build();
                        })
                        .collect(Collectors.toList());



        // 리소스 그룹에 속하는 사용자 수
        DashboardDto.UserCountResponse userCount =
                reservationFeignAdapter.getUserCount(resourceGroupId, companyId).getData();
        // 성별
        List<DashboardDto.ReservationGenderInfo> genderCount =
                reservationFeignAdapter.getGenderCount(resourceGroupId).getData();
        // 나이
        List<DashboardDto.ReservationAgeInfo> ageCount =
                reservationFeignAdapter.getAgeCount(resourceGroupId).getData();
        // 시간대 별 예약 수
        List<DashboardDto.TimeSlotReservationCount> hourlyReservationCounts =
                reservationFeignAdapter.getHourlyReservationCount(resourceGroupId).getData();


        // 최종 응답 DTO 생성
        return DashboardDto.ResourceGroupDashboardData.builder()
                .resourceCount(resourceData.getResourceCount())
                .cumReservationCount(cumReservationCount)
                .cumCancleCount(cumCancalCount)
                .totalCustomerCount(userCount.getTotal())
                .useCustomerCount(userCount.getCount())
                .performanceByResources(performancePerResources)
                .reservationGenderInfos(genderCount)
                .reservationAgeInfos(ageCount)
                .yesterDayViewCount(resourceData.getViewStats().getYesterdayAccumulatedViewCount())
                .todayViewCount(resourceData.getViewStats().getTodayTotalViewCount())
                .hourlyViewCounts(resourceData.getViewStats().getHourlyViewCounts())
                .houlryReservationCounts(hourlyReservationCounts)
                .build();

    }
}