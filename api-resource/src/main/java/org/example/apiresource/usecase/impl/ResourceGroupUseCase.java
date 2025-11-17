package org.example.apiresource.usecase.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.domain.model.entity.*;
import org.example.apiresource.domain.service.ResourceGroupService;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.apiresource.usecase.port.out.*;
import org.example.common.model.dto.AuthDto;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceGroupUseCase implements ResourceGroupWebPort {
    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupPersistencePort resourceGroupPersistencePort;
    private final ResourcePersistencePort resourcePersistencePort;
    private final ViewCountPersistencePort viewCountPersistencePort;
    private final ResourceTimeSlotPersistencePort resourceTimeSlotPersistencePort;
    private final ResourceTimeSlotExceptionPersistencePort resourceTimeSlotExceptionPersistencePort;


    // ---------------------  리소스 그룹 생성 -----------------------------
    @Override
    @Transactional
    public void register(ResourceGroupDto.ResourceGroupRegisterReq dto, Long userId, Long companyId) {

        // ResourceGroup 엔티티 생성
        ResourceGroups resourceGroup = resourceGroupService.createResourceGroup(dto, userId, companyId);

        // ResourceGroup 저장 (ID 확보)
        resourceGroupPersistencePort.saveResourceGroup(resourceGroup);

        // 커스텀 필드 생성 + 저장 + 옵션 처리
        List<CustomFieldDto.CustomFieldReq> fieldDtos = dto.getCustomFields();
        if (fieldDtos != null && !fieldDtos.isEmpty()) {
            for (CustomFieldDto.CustomFieldReq fieldDto : fieldDtos) {

                // 커스텀 필드 엔티티 생성
                CustomFieldDefinitions field = resourceGroupService.createCustomFieldEntity(fieldDto, resourceGroup);

                // DB 저장 → ID 확보
                resourceGroupPersistencePort.saveCustomField(field);

                // 옵션 처리 (RADIO / CHECKBOX만)
                if (field.getDataType() == CustomDataType.RADIO ||
                        field.getDataType() == CustomDataType.CHECKBOX) {

                    List<String> options = fieldDto.getOptions();
                    if (options != null && !options.isEmpty()) {
                        for (String optionName : options) {
                            CustomFieldSelectDefinitions option =
                                    resourceGroupService.createOptionEntity(optionName, field);
                            resourceGroupPersistencePort.saveOption(option);
                        }
                    }
                }
            }
        }
    }



    // ---------------------  기업별 모든 리소스 그룹 조회 -----------------------------
    @Override
    @Transactional(readOnly = true)
    public ResourceGroupDto.ResourceGroupListRes getResourceGroupsByCompanyId(UserRole role, Long companyId) {
        // User면 활성화된 서비스만, ADMIN 등은 모두
        boolean isUser = (role == UserRole.USER);

        // port.out을 통해 DB 접근
        List<ResourceGroups> groups = resourceGroupPersistencePort.findAllByCompanyIdAndRole(companyId, isUser);

        // domain.service에 DTO 변환 위임
        return resourceGroupService.toListResponse(groups);
    }



    // --------------------- 리소스 그룹 단일 조회 -----------------------------
    @Override
    @Transactional(readOnly = true)
    public ResourceGroupDto.ResourceGroupDetailRes getResourceGroupById(UserRole userRole, Long resourceGroupId) {
        // Port Out 통해 DB 조회
        ResourceGroups group = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 서비스 그룹이 존재하지 않습니다."));

        // USER라면 조회수 증가
        if (userRole == UserRole.USER) {
            resourceGroupPersistencePort.incrementViewCount(resourceGroupId);
        }

        return resourceGroupService.toResourceGroupInfo(group);
    }


    // --------------------- 리소스 그룹 단일 조회(수정용) -----------------------------
    @Override
    @Transactional
    public ResourceGroupDto.ResourceGroupUpdateRes getResourceGroupUpdateDetail(Long resourceGroupId) {
        ResourceGroups group = resourceGroupPersistencePort.findByIdAndDeletedAtIsNull(resourceGroupId);

        return ResourceGroupService.toResourceGroupUpdateInfo(group);
    }


    // --------------------- 리소스 그룹 수정 -----------------------------
    @Override
    @Transactional
    public void updateResourceGroup(Long userId, Long resourceGroupId, ResourceGroupDto.ResourceGroupUpdateReq dto) {
        // Port Out 통해 DB 조회
        ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        // Domain Service에서 커스텀 필드 처리 및 엔티티 업데이트
        resourceGroupService.updateResourceGroup(resourceGroup, userId, dto);
    }


    // --------------------- 리소스 그룹 삭제 -----------------------------
    @Override
    @Transactional
    public void deleteResourceGroup(Long id, Long resourceGroupId) {
        ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        resourceGroupService.deleteResourceGroup(resourceGroup, id);
    }


    // --------------------- 서비스 그룹 카테고리 & 상시 모집 여부 조회 -----------------------------
    @Override
    @Transactional
    public ResourceGroupDto.ServiceRegisterFieldRes getServiceRegisterField(Long resourceGroupId) {
        ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        return resourceGroupService.getServiceFields(resourceGroup);
    }


    // --------------------- 서비스 그룹 활성화 -----------------------------
    @Override
    @Transactional
    public void activate(Long userId, UserRole userRole, Long resourceGroupId) {
        try {
            if (userRole != UserRole.SUPER) {
                throw new IllegalArgumentException("플랫폼 관리자만 서비스 그룹 상태를 변경할 수 있습니다.");
            }

            ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            resourceGroupService.activate(resourceGroup, userId);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // --------------------- 서비스 그룹 비활성화 -----------------------------
    @Override
    @Transactional
    public void deactivate(Long userId, UserRole userRole, Long resourceGroupId) {
        try {
            if (userRole != UserRole.SUPER) {
                throw new IllegalArgumentException("플랫폼 관리자만 서비스 그룹 상태를 변경할 수 있습니다.");
            }

            ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            resourceGroupService.deactivate(resourceGroup, userId);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // --------------------- 관리자 전체 대시보드에 필요한 데이터 조회 -----------------------------
    @Override
    @Transactional
    public ResourceGroupDto.AdminDashboardResourceGroup getAdminTotalDashboard(Long companyId) {
        // 1. 기업에 속한 모든 리소스 그룹 조회 (삭제되지 않은 것만)
        List<ResourceGroups> groups = resourceGroupPersistencePort
                .findAllByCompanyIdAndDeletedAtIsNull(companyId);

        // 2. 그룹 정보를 DTO 로 변환
        List<ResourceGroupDto.AdminDashboardGroupInfo> groupInfos = groups.stream()
                .map(group -> ResourceGroupDto.AdminDashboardGroupInfo.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .viewCount(group.getViewCount())
                        .isActive(group.getIsActive())
                        .serviceCount(resourcePersistencePort.countAllByResourceGroup_Id(group.getId()))
                        .build()
                )
                .toList();

        // 3. 전체 리소스 개수 조회
        int totalResourceCount = resourcePersistencePort.countAllByResourceGroup_CompanyId(companyId);

        // 4. 최종 DTO 조합 후 반환
        return ResourceGroupDto.AdminDashboardResourceGroup.builder()
                .groups(groupInfos)
                .groupCount(groupInfos.size())
                .resourceCount(totalResourceCount)
                .build();
    }



    // --------------------- 플랫폼 관리자 전체 대시보드에 필요한 데이터 조회 -----------------------------
    @Override
    @Transactional
    public ResourceGroupDto.ServiceStatsResponse getSuperTotalDashboard() {
        // 1. 활성 + 미삭제 리소스 그룹 조회
        List<ResourceGroups> activeGroups =
                resourceGroupPersistencePort.findAllByIsActiveTrueAndDeletedAtIsNull();

        int totalServiceCount = activeGroups.size();

        // 2. 카테고리 라벨 (ALL 제외)
        ServiceCategory[] categories = Arrays.stream(ServiceCategory.values())
                .filter(c -> c != ServiceCategory.ALL)
                .toArray(ServiceCategory[]::new);

        List<String> categoryLabels = Arrays.stream(categories)
                .map(ServiceCategory::getLabel)
                .toList();

        // 3. 카테고리별 그룹 개수 (리소스 그룹 기준)
        List<Integer> categoryCounts = Arrays.stream(categories)
                .map(category ->
                        resourceGroupPersistencePort
                                .countAllByCategoryAndIsActiveTrueAndDeletedAtIsNull(category)
                )
                .toList();

        // 4. 응답 생성
        return ResourceGroupDto.ServiceStatsResponse.builder()
                .totalServiceCount(totalServiceCount)
                .categoryCounts(categoryCounts)
                .categoryLabels(categoryLabels)
                .build();
    }


    // 리소스 그룹 대시보드 관련해서 필요한 대시보드 데이터 조회
    @Override
    @Transactional
    public ResourceGroupDto.ResourceGroupDashboardResponse getResourceGroupDashboard(Long resourceGroupId) {

        // 1. 리소스 그룹 조회
        ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        // 2. 이 그룹에 속한 리소스 목록 조회
        List<Resources> resources = resourcePersistencePort
                .findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(resourceGroupId);

        int resourceCount = resources.size();

        // 3. 리소스별 예약 가능 시간 개수 계산
        List<ResourceGroupDto.ResourcePossibleTimeInfo> resourceInfos = resources.stream()
                .map(resource -> {
                    int interval = resource.getTimeInterval();  // ex) 30분

                    // ---------------------------
                    // A. 이번 달 날짜/요일 개수 계산
                    // ---------------------------
                    LocalDate today = LocalDate.now();
                    YearMonth ym = YearMonth.from(today);
                    int daysInMonth = ym.lengthOfMonth();

                    Map<DayOfWeek, Integer> dayCountMap = new EnumMap<>(DayOfWeek.class);
                    for (DayOfWeek d : DayOfWeek.values()) dayCountMap.put(d, 0);

                    for (int d = 1; d <= daysInMonth; d++) {
                        LocalDate date = ym.atDay(d);
                        dayCountMap.computeIfPresent(date.getDayOfWeek(), (k, v) -> v + 1);
                    }

                    // ---------------------------
                    // B. 요일별 정규 슬롯 is_active 개수 조회
                    // ---------------------------
                    Map<org.example.apiresource.domain.model.DayOfWeek, Integer> regularActiveSlots =
                            resourceTimeSlotPersistencePort.countActiveSlotsByResource(resource.getId());

                    // 이번 달 전체 정규 슬롯 개수
                    int totalRegularSlots = regularActiveSlots.entrySet().stream()
                            .mapToInt(e -> e.getValue() * dayCountMap.getOrDefault(e.getKey(), 0))
                            .sum();

                    // ---------------------------
                    // C. 예외시간 적용
                    // ---------------------------
                    List<ResourceTimeSlotExceptions> exceptions =
                            resourceTimeSlotExceptionPersistencePort.findByResourceId(resource.getId());

                    int totalAdjusted = totalRegularSlots;

                    // 날짜별로 묶기
                    Map<LocalDate, List<ResourceTimeSlotExceptions>> byDate =
                            exceptions.stream().collect(Collectors.groupingBy(ResourceTimeSlotExceptions::getDate));

                    for (LocalDate date : byDate.keySet()) {

                        List<ResourceTimeSlotExceptions> list = byDate.get(date);
                        DayOfWeek dow = date.getDayOfWeek();

                        // 정규 슬롯 제거
                        int regularOfDay = regularActiveSlots.getOrDefault(dow, 0);
                        totalAdjusted -= regularOfDay;

                        // 휴무면 끝
                        boolean isClosed = list.stream().anyMatch(ResourceTimeSlotExceptions::getIsClosed);
                        if (isClosed) continue;

                        // 시간대별 추가 슬롯 계산
                        int restore = 0;

                        for (ResourceTimeSlotExceptions ex : list) {
                            long minutes = Duration.between(ex.getStartTime(), ex.getEndTime()).toMinutes();
                            restore += (int) (minutes / interval);
                        }

                        totalAdjusted += restore;
                    }

                    // ---------------------------
                    // D. DTO 생성
                    // ---------------------------
                    return ResourceGroupDto.ResourcePossibleTimeInfo.builder()
                            .resourceId(resource.getId())
                            .resourceName(resource.getName())
                            .intervalMinutes(interval)
                            .possibleTimeCount(totalAdjusted)
                            .build();
                })
                .collect(Collectors.toList());

        // ---------------------------
        // 4. 조회수 통계
        // ---------------------------
        LocalDateTime nowDateTime = LocalDateTime.now();

        // 어제 누적 조회수
        LocalDateTime yesterdayStart = nowDateTime.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime yesterdayEnd = yesterdayStart.plusHours(nowDateTime.getHour())
                .plusMinutes(nowDateTime.getMinute())
                .plusSeconds(nowDateTime.getSecond());

        long yesterdayAccumulated = viewCountPersistencePort
                .sumViewsByResourceGroupIdAndDate(resourceGroupId, yesterdayStart, yesterdayEnd);

        // 오늘 전체 조회수
        LocalDateTime todayStart = nowDateTime.toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = nowDateTime;

        long todayTotal = viewCountPersistencePort
                .sumViewsByResourceGroupIdAndDate(resourceGroupId, todayStart, todayEnd);


        // 시간대별 조회수
        List<Object[]> results = viewCountPersistencePort.getTodayHourlyViews(
                resourceGroupId,
                todayStart,
                todayEnd
        );

        Map<Integer, Long> hourMap = new HashMap<>();
        for (Object[] row : results) {
            Integer hour = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            hourMap.put(hour, count);
        }

// 시간대별 조회수 리스트 생성 (현재 시각까지만)
        List<ResourceGroupDto.HourlyViewCount> hourlyViewCounts = new ArrayList<>();
        for (int h = 0; h <= nowDateTime.getHour(); h++) {
            hourlyViewCounts.add(
                    ResourceGroupDto.HourlyViewCount.builder()
                            .hour(h)
                            .viewCount(hourMap.getOrDefault(h, 0L))
                            .build()
            );
        }

        // ---------------------------
        // 5. ViewStats DTO 생성
        // ---------------------------
        ResourceGroupDto.ViewStats viewStats = ResourceGroupDto.ViewStats.builder()
                .yesterdayAccumulatedViewCount(yesterdayAccumulated)
                .todayTotalViewCount(todayTotal)
                .hourlyViewCounts(hourlyViewCounts)
                .build();

        // ---------------------------
        // 6. 최종 응답 DTO 생성
        // ---------------------------
        return ResourceGroupDto.ResourceGroupDashboardResponse.builder()
                .resourceGroupId(resourceGroupId)
                .resourceCount(resourceCount)
                .resources(resourceInfos)
                .viewStats(viewStats)
                .build();
    }



}