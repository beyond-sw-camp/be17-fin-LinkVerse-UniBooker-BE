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
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.example.unibooker.domain.user.model.Gender;
import java.time.Year;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

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
                    CompanyStatus.ACTIVE,
                    monthStart.atStartOfDay(),
                    monthEnd.atTime(LocalTime.MAX)
            );
            monthlyNewCompanies.add(count);
        }

        DashboardDto.CompanyStats companyStats = DashboardDto.CompanyStats.builder()
                .currentCompanyCount(companyRepository.findByStatus(CompanyStatus.ACTIVE).size())
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

    /**
     * 리소스 그룹별 대시보드 조회
     */
    public DashboardDto.ResourceGroupDashboardResponse getResourceGroupDashboard(Long resourceGroupId, Long companyId) {
        // 리소스 그룹 조회 및 권한 검증
        ResourceGroups resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_GROUP_NOT_FOUND));

        if (!resourceGroup.getCompany().getId().equals(companyId)) {
            throw new BaseException(BaseResponseStatus.FORBIDDEN);
        }

        // 해당 그룹의 리소스 목록
        // 수정된 코드 (count 쿼리 직접 사용)
        int resourceCount = resourceRepository.countByResourceGroupIdAndDeletedAtIsNull(resourceGroupId);

        // 2. 누적 예약 수
        int cumReservationCount = reservationRepository.countByResourceGroupId(resourceGroupId);

        // 3. 누적 취소 수
        int cumCancleCount = reservationRepository.countCancelledByResourceGroupId(resourceGroupId);

        // 4. 이용자 수 (예약한 고유 사용자 수)
        int useCustomerCount = reservationRepository.countDistinctUsersByResourceGroupId(resourceGroupId);

        // 5. 전체 고객 수 (회사 소속 USER 수)
        int totalCustomerCount = userRepository.findAllByCompany_IdAndRole(companyId, UserRole.USER).size();

        // 6. 서비스별 성과 (한 달 기준)
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Object[]> performanceRaw = reservationRepository.countByResourceInPeriod(resourceGroupId, oneMonthAgo);
        int totalReservationsInMonth = performanceRaw.stream()
                .mapToInt(row -> ((Number) row[1]).intValue())
                .sum();

        List<DashboardDto.ResourcePerformance> performanceByResources = performanceRaw.stream()
                .map(row -> DashboardDto.ResourcePerformance.builder()
                        .resourceName((String) row[0])
                        .count(totalReservationsInMonth > 0
                                ? ((Number) row[1]).doubleValue() / totalReservationsInMonth * 100
                                : 0)
                        .build())
                .toList();

        // 7. 오늘/어제 조회 수
        int todayViewCount = resourceGroup.getViewCount(); // 실제로는 일별 로그 필요
        int yesterDayViewCount = 0; // 실제로는 access_logs 테이블 조회 필요

        // 8. 시간별 예약 수 (오늘 기준)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<Object[]> hourlyReservationRaw = reservationRepository.countByHour(resourceGroupId, todayStart, todayEnd);
        List<DashboardDto.HourlyCount> houlryReservationCounts = hourlyReservationRaw.stream()
                .map(row -> DashboardDto.HourlyCount.builder()
                        .hour(((Number) row[0]).intValue())
                        .count(((Number) row[1]).intValue())
                        .build())
                .toList();

        // 9. 시간별 조회 수 (실제로는 access_logs 테이블 조회 필요)
        List<DashboardDto.HourlyViewCount> hourlyViewCounts = List.of();

        // 10. 성별 통계
        DashboardDto.GenderStats genderStats = calculateGenderStats(companyId);

        // 11. 연령대 통계
        List<DashboardDto.AgeGroupStats> ageGroupStats = calculateAgeGroupStats(companyId);

        return DashboardDto.ResourceGroupDashboardResponse.builder()
                .resourceCount(resourceCount)
                .cumReservationCount(cumReservationCount)
                .cumCancleCount(cumCancleCount)
                .useCustomerCount(useCustomerCount)
                .totalCustomerCount(totalCustomerCount)
                .performanceByResources(performanceByResources)
                .todayViewCount(todayViewCount)
                .yesterDayViewCount(yesterDayViewCount)
                .houlryReservationCounts(houlryReservationCounts)
                .hourlyViewCounts(hourlyViewCounts)
                .genderStats(genderStats)
                .ageGroupStats(ageGroupStats)
                .build();
    }

    /**
     * 성별 통계 계산
     */
    private DashboardDto.GenderStats calculateGenderStats(Long companyId) {
        List<Object[]> genderData = userRepository.countByGenderAndCompanyId(companyId);

        int maleCount = 0;
        int femaleCount = 0;
        int undefinedCount = 0;

        for (Object[] row : genderData) {
            Gender gender = (Gender) row[0];
            int count = ((Number) row[1]).intValue();

            if (gender == null || gender == Gender.UNDEFINED) {
                undefinedCount += count;
            } else if (gender == Gender.MALE) {
                maleCount = count;
            } else if (gender == Gender.FEMALE) {
                femaleCount = count;
            }
        }

        int total = maleCount + femaleCount + undefinedCount;

        return DashboardDto.GenderStats.builder()
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .undefinedCount(undefinedCount)
                .malePercent(total > 0 ? Math.round(maleCount * 1000.0 / total) / 10.0 : 0)
                .femalePercent(total > 0 ? Math.round(femaleCount * 1000.0 / total) / 10.0 : 0)
                .undefinedPercent(total > 0 ? Math.round(undefinedCount * 1000.0 / total) / 10.0 : 0)
                .build();
    }

    /**
     * 연령대 통계 계산
     */
    private List<DashboardDto.AgeGroupStats> calculateAgeGroupStats(Long companyId) {
        List<Users> users = userRepository.findUsersWithBirthDateByCompanyId(companyId);

        // 연령대별 카운트
        Map<String, Integer> ageGroupCount = new LinkedHashMap<>();
        ageGroupCount.put("10대", 0);
        ageGroupCount.put("20대", 0);
        ageGroupCount.put("30대", 0);
        ageGroupCount.put("40대", 0);
        ageGroupCount.put("50대", 0);
        ageGroupCount.put("60대 이상", 0);

        int currentYear = Year.now().getValue();

        for (Users user : users) {
            int age = calculateAge(user.getBirthDate(), currentYear);
            String ageGroup = getAgeGroup(age);
            ageGroupCount.merge(ageGroup, 1, Integer::sum);
        }

        int total = users.size();

        return ageGroupCount.entrySet().stream()
                .map(entry -> DashboardDto.AgeGroupStats.builder()
                        .ageGroup(entry.getKey())
                        .count(entry.getValue())
                        .percent(total > 0 ? Math.round(entry.getValue() * 1000.0 / total) / 10.0 : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 생년월일로 나이 계산
     */
    private int calculateAge(String birthDate, int currentYear) {
        if (birthDate == null || birthDate.isBlank()) {
            return 0;
        }

        try {
            // "1990-01-15" 또는 "19900115" 형식 지원
            String yearStr = birthDate.length() >= 4 ? birthDate.substring(0, 4) : birthDate;
            int birthYear = Integer.parseInt(yearStr.replaceAll("[^0-9]", "").substring(0, 4));
            return currentYear - birthYear;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 나이로 연령대 문자열 반환
     */
    private String getAgeGroup(int age) {
        if (age < 20) return "10대";
        if (age < 30) return "20대";
        if (age < 40) return "30대";
        if (age < 50) return "40대";
        if (age < 60) return "50대";
        return "60대 이상";
    }

}
