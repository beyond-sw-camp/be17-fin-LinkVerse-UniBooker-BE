package org.example.unibooker.domain.resource.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.notification.service.NotificationService;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.*;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final ResourceCustomFieldValueRepository resourceCustomFieldValueRepository;
    private final ResourceTimeSlotRepository resourceTimeSlotRepository;
    private final ResourceTimeSlotExceptionRepository resourceTimeSlotExceptionRepository;
    private final NotificationService notificationService;


    // -------------------- 리소스 등록 --------------------
    @Transactional
    public void register(ResourceDto.ResourceRegisterReq dto, Long userId) {
        dto.validate();

        ResourceGroups group = resourceGroupRepository.findById(dto.getResourceGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        // 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID입니다."));

        Resources resource = dto.toEntity(group, user);
        resourceRepository.save(resource);

        // 시간 슬롯 생성
        if (group.getCategory() != ServiceCategory.EVENT) {
            int intervalMinutes = dto.getTimeInterval();
            int slotsPerDay = (24 * 60) / intervalMinutes;

            for (DayOfWeek day : DayOfWeek.values()) {
                for (int i = 0; i < slotsPerDay; i++) {
                    LocalTime slotStart = LocalTime.of(0, 0).plusMinutes((long) i * intervalMinutes);
                    LocalTime slotEnd = slotStart.plusMinutes(intervalMinutes);

                    if (slotEnd.equals(LocalTime.MIDNIGHT)) {
                        slotEnd = LocalTime.of(23, 59, 59);
                    }

                    boolean active = false;

                    if (dto.getTimeSlots() != null) {
                        for (TimeSlotDto.TimeSlotRequest slotDto : dto.getTimeSlots()) {
                            if (slotDto.getDays() != null) {
                                for (DayOfWeek dayEnum : slotDto.getDays()) { // 이미 Enum
                                    if (dayEnum == day) { // Enum 비교
                                        LocalTime targetStart = slotDto.getStartTime();
                                        LocalTime targetEnd = slotDto.getEndTime();

                                        if ((slotStart.equals(targetStart) || slotStart.isAfter(targetStart))
                                                && slotStart.isBefore(targetEnd)) {
                                            active = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (active) break;
                        }
                    }


                    ResourceTimeSlots slot = ResourceTimeSlots.builder()
                            .resources(resource)
                            .dayOfWeek(day)
                            .startTime(slotStart)
                            .endTime(slotEnd)
                            .isActive(active)
                            .build();

                    resource.addTimeSlot(slot);
                }
            }
        }


        // 예외 시간 슬롯 생성
        if (dto.getExceptionSlots() != null && !dto.getExceptionSlots().isEmpty()) {
            for (TimeSlotDto.ExceptionSlotRequest exDto : dto.getExceptionSlots()) {

                if (!exDto.getIsClosed() && (exDto.getStartTime() == null || exDto.getEndTime() == null)) {
                    throw new IllegalArgumentException("휴무가 아닐 경우 시작시간과 종료시간은 필수입니다. 날짜: " + exDto.getDate());
                }

                resource.addTimeSlotException(exDto.toEntity(resource));
            }
        }

        // 커스텀 필드 저장
        if (dto.getCustomFieldValues() != null && !dto.getCustomFieldValues().isEmpty()) {
            for (CustomFieldDto.CustomFieldValue customValueDto : dto.getCustomFieldValues()) {
                CustomFieldDefinitions field = customFieldDefinitionRepository
                        .findByIdAndDeletedAtIsNull(customValueDto.getCustomFieldId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + customValueDto.getCustomFieldId()));

                if (field.getTargetType() == CustomTargetType.RESOURCE) {
                    List<ResourceCustomFieldValues> values =
                            customValueDto.toResourceEntities(field, resource.getId());
                    for (ResourceCustomFieldValues value : values) {
                        resourceCustomFieldValueRepository.save(value);
                    }
                }
            }
        }
    }


    // -------------------- 리소스 목록 조회 --------------------
    public ResourceDto.ResourceListRes getAllResourcesByGroupId(Long serviceGroupId) {
        List<Resources> resources = resourceRepository
                .findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(serviceGroupId);

        List<ResourceDto.ResourceDetailInfo> resourceInfos = resources.stream()
                .map(ResourceDto.ResourceDetailInfo::fromEntity)
                .collect(Collectors.toList());

        return ResourceDto.ResourceListRes.fromEntity(resourceInfos);
    }


    // -------------------- 리소스 상세 조회 (목록 조회용) --------------------
    public ResourceDto.ResourceDetailInfo getResourceById(Long resourceId) {
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return ResourceDto.ResourceDetailInfo.fromEntity(resource);
    }

    // -------------------- 리소스 상세 조회 (비관적 락) --------------------
    public ResourceDto.ResourceDetailInfo getPessimisticResourceById(Long resourceId) {
        Resources resource = resourceRepository.findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return ResourceDto.ResourceDetailInfo.fromEntity(resource);
    }


    // -------------------- 리소스 수정 --------------------
    @Transactional
    public void update(Long userId, Long resourceId, ResourceDto.ResourceUpdateReq dto) {

        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스가 존재하지 않습니다."));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

        // 리소스 기본 정보 업데이트
        resource.update(dto, user);

        // 정규 시간 슬롯 업데이트
        List<ResourceTimeSlots> allSlots = resourceTimeSlotRepository.findByResources_Id(resourceId);

        List<TimeSlotDto.TimeSlotRequest> dtoSlots = dto.getTimeSlots();

        for (ResourceTimeSlots slot : allSlots) {
            boolean active = false;

            if (dtoSlots != null && !dtoSlots.isEmpty()) {
                for (TimeSlotDto.TimeSlotRequest ts : dtoSlots) {
                    if (ts.getDays().contains(slot.getDayOfWeek())) {
                        // DTO 범위 안에 slot이 들어있으면 활성화
                        if (!slot.getStartTime().isBefore(ts.getStartTime()) && !slot.getEndTime().isAfter(ts.getEndTime())) {
                            active = true;
                            break;
                        }
                    }
                }
            }

            slot.setIsActive(active);
        }



        // 예외 시간 슬롯 업데이트
        if (dto.getExceptionSlots() != null && !dto.getExceptionSlots().isEmpty()) {
            // 기존 예외 슬롯 소프트 삭제
            List<ResourceTimeSlotExceptions> existingExceptions =
                    resourceTimeSlotExceptionRepository.findByResources_Id(resourceId);
            existingExceptions.forEach(ResourceTimeSlotExceptions::softDelete);

            // 새로운 예외 슬롯 추가
            for (TimeSlotDto.ExceptionSlotRequest exDto : dto.getExceptionSlots()) {
                ResourceTimeSlotExceptions ex = ResourceTimeSlotExceptions.builder()
                        .resources(resource)
                        .date(exDto.getDate())
                        .startTime(exDto.getIsClosed() ? null : exDto.getStartTime())
                        .endTime(exDto.getIsClosed() ? null : exDto.getEndTime())
                        .isClosed(exDto.getIsClosed())
                        .note(exDto.getNote())
                        .build();
                resourceTimeSlotExceptionRepository.save(ex);
            }
        } else {
            // DTO에 예외 슬롯이 없으면 기존 예외 슬롯 소프트 삭제
            List<ResourceTimeSlotExceptions> existingExceptions =
                    resourceTimeSlotExceptionRepository.findByResources_Id(resourceId);
            existingExceptions.forEach(ResourceTimeSlotExceptions::softDelete);
        }

    }


    // -------------------- 리소스 삭제 --------------------
    @Transactional
    public void deleteResource(Long resourceId, Long userId) {
        try {
            Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스가 존재하지 않습니다."));

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

            if (!Boolean.TRUE.equals(resource.getIsActive())) {
                throw new IllegalArgumentException("이미 비활성화 또는 삭제된 리소스입니다.");
            }

            // 리소스 비활성화 및 소프트 삭제
            resource.setIsActive(false);
            resource.setUpdatedBy(user);
            resource.softDelete();

            // 연관 정규 시간 슬롯 소프트 삭제
            List<ResourceTimeSlots> allSlots = resourceTimeSlotRepository.findByResources_Id(resourceId);
            allSlots.forEach(ResourceTimeSlots::softDelete);

            // 연관 예외 시간 슬롯 소프트 삭제
            List<ResourceTimeSlotExceptions> exceptionSlots = resourceTimeSlotExceptionRepository.findByResources_Id(resourceId);
            exceptionSlots.forEach(ResourceTimeSlotExceptions::softDelete);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", e);
        }
    }


    // -------------------- 리소스 활성화 --------------------
    @Transactional
    public void activate(Long resourceId, Long userId) {

        try {
            Resources resource = resourceRepository.findByIdAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

            if (Boolean.TRUE.equals(resource.getIsActive())) {
                log.info("이미 활성화된 서비스입니다. id={}", resourceId);
                return;
            }

            resource.setIsActive(true);
            resource.setUpdatedBy(user);

            log.info("서비스 활성화 완료 - id={}", resourceId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // -------------------- 리소스 비활성화 --------------------
    @Transactional
    public void deactivate(Long resourceId, Long userId) {

        try {
            Resources resource = resourceRepository.findByIdAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

            if (Boolean.FALSE.equals(resource.getIsActive())) {
                log.info("이미 활성화된 서비스입니다. id={}", resourceId);
                return;
            }

            resource.setIsActive(false);
            resource.setUpdatedBy(user);

            log.info("서비스 비활성화 완료 - id={}", resourceId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }

    /**
     * 상태 변경이 가능한지 판단
     */
    private boolean isStatusChangeable(Resources resource, ResourceStatus targetStatus){
        ResourceStatus currentStatus = resource.getStatus();
        LocalDate today = LocalDate.now();
        boolean always = resource.getResourceGroup() != null
                && Boolean.TRUE.equals(resource.getResourceGroup().getIsAlwaysAvailable());

        // 상시모집 (기간 개념 없음)
        if (always) {
            if (targetStatus == ResourceStatus.PROGRESS_BEFORE) return false; // 상시모집은 진행전 상태 없음
            // 진행중 ↔ 종료만 가능
            return (currentStatus == ResourceStatus.IN_PROGRESS && targetStatus == ResourceStatus.CLOSED)
                    || (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.IN_PROGRESS);
        }

        // 기간형
        LocalDate start = resource.getStartDate();
        LocalDate end = resource.getEndDate();

        // 날짜 정보가 없으면 '닫기'만 허용
        if (start == null || end == null) {
            return targetStatus == ResourceStatus.CLOSED;
        }

        // → CLOSED : 항상 허용
        if (targetStatus == ResourceStatus.CLOSED) return true;

        // CLOSED → IN_PROGRESS : 오늘이 기간 내에 있어야 함
        if (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.IN_PROGRESS) {
            return !today.isBefore(start) && !today.isAfter(end);
        }

        // CLOSED → PROGRESS_BEFORE : 오늘이 시작 전이어야 함
        if (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.PROGRESS_BEFORE) {
            return today.isBefore(start);
        }

        // 기간이 지났으면 재오픈 불가
        if (today.isAfter(end)) return false;

        // 기타 케이스 불허
        return false;
    }

    @Transactional
    public boolean changeStatus(AuthDto.AuthenticatedUser authUser, ResourceDto.ResourceStatusChangReq req) {
        Resources resource = resourceRepository.findById(req.getResourceId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));
        Users updatedBy = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 낙관적 락 검증
        if (req.getVersion() != null && !resource.getVersion().equals(req.getVersion())) {
            throw new BaseException(BaseResponseStatus.CONCURRENT_MODIFICATION);
        }

        ResourceStatus targetStatus = ResourceStatus.valueOf(req.getTargetStatus());

        // 변경 가능 여부 판단
        if (!isStatusChangeable(resource, targetStatus)) return false;

        // 변경 반영
        resource.setStatus(targetStatus);
        resource.setUpdatedBy(updatedBy);

        return true;
    }
}