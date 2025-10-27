package org.example.unibooker.domain.analytics.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.common.exception.GlobalExceptionHandler;
import org.example.unibooker.domain.analytics.model.DashboardDto;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ResourceGroupRepository resourceGroupRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;

    public DashboardDto.AdminDashboardResponse getCompanyDashboard(Long companyId) {
        // 해당 회사(companyId)에 속한 리소스 그룹 목록 조회
        List<ResourceGroups> resourceGroups = resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);

        //  각 리소스 그룹별 예약 수, 리소스 수, 조회 수 집계
        int totalReservations = reservationRepository.countByCompanyId(companyId); // 회사 전체 예약
        int activeServiceGroups = resourceGroups.size(); // 활성화된 그룹 수
        int activeServices = resourceRepository.countActiveResourcesByCompanyId(companyId); // 활성 리소스 총합
        int userCount = userRepository.findAllByCompanyIdAndRole(companyId, UserRole.USER).size(); // 해당 회사에 소속된 사용자 수


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
            LocalDate date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
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

    public DashboardDto.SuperDashboardResponse getPlatformDashboard(AuthDto.AuthenticatedUser authUser) {
        if (authUser.getRole() != UserRole.SUPER) { // 권한 검증
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        // 현재 날짜 기준 설정
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);

        /** ------------------ 기업 통계 ------------------ **/
        List<Integer> monthlyNewCompanies = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = startOfYear.withMonth(month).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            int count = companyRepository.countAllByStatusAndApprovedAtBetween(
                    CompanyStatus.APPROVED,
                    monthStart.atStartOfDay(),
                    monthEnd.atTime(LocalTime.MAX)
            );
            monthlyNewCompanies.add(count);
        }

        DashboardDto.CompanyStats companyStats = DashboardDto.CompanyStats.builder()
                .currentCompanyCount(companyRepository.findByStatus(CompanyStatus.APPROVED).size())
                .monthlyNewRegistrations(monthlyNewCompanies)
                .build();

        /** ------------------ 고객 통계 ------------------ **/
        List<Integer> cumulativeRegistrations = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate monthEnd = startOfYear.withMonth(month).withDayOfMonth(
                    startOfYear.withMonth(month).lengthOfMonth()
            );

            int monthlyCount = userRepository.countAllByRoleAndCreatedAtBefore(
                    UserRole.USER,
                    monthEnd.atTime(LocalTime.MAX)
            );
            cumulativeRegistrations.add(monthlyCount);
        }

        DashboardDto.CustomerStats customerStats = DashboardDto.CustomerStats.builder()
                .currentCustomerCount(
                        userRepository.countAllByRoleAndStatus(UserRole.USER, UserStatus.ACTIVE)
                )
                .cumulativeRegistrations(cumulativeRegistrations)
                .build();

        /** ------------------ 서비스 통계 ------------------ **/
        ServiceCategory[] categories = Arrays.stream(ServiceCategory.values())
                .filter(category -> category != ServiceCategory.ALL) // ALL 제외
                .toArray(ServiceCategory[]::new);

        List<String> categoryLabels = Arrays.stream(categories)
                .map(ServiceCategory::getLabel)
                .toList();

        List<Integer> categoryCounts = Arrays.stream(categories)
                .map(resourceRepository::countAllByIsActiveIsTrueAndResourceGroup_Category)
                .toList();

        DashboardDto.ServiceStats serviceStats = DashboardDto.ServiceStats.builder()
                .totalServiceCount(resourceRepository.countAllByIsActive(true)) // 전체 활성 리소스 수
                .categoryCounts(categoryCounts)
                .categoryLabels(categoryLabels)
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

}
