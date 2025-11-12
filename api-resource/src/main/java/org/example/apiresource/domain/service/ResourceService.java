package org.example.apiresource.domain.service;

import lombok.AllArgsConstructor;
import org.example.apiresource.domain.model.ResourceStatus;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class ResourceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // 입력값 검증
    public void validate(ResourceDto.ResourceRegisterReq dto) {
        if (dto.getEndDate() != null && dto.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("종료일이 오늘 이전인 리소스는 생성할 수 없습니다.");
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null &&
                dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        if (dto.getTimeInterval() != 30 && dto.getTimeInterval() != 60) {
            throw new IllegalArgumentException("timeInterval은 30 또는 60만 가능합니다.");
        }
    }


    // 리소스 등록값을 엔티티로 변환
    public Resources createResource(ResourceDto.ResourceRegisterReq resource, ResourceGroups group, Long userId) {
        LocalDate today = LocalDate.now();

        // 상태 결정
        ResourceStatus status;
        boolean alwaysAvailable = Boolean.TRUE.equals(group.getIsAlwaysAvailable());
        boolean startDatePassed = resource.getStartDate() != null && !resource.getStartDate().isAfter(today);

        if (alwaysAvailable || startDatePassed) {
            status = ResourceStatus.IN_PROGRESS;
        } else {
            status = ResourceStatus.PROGRESS_BEFORE;
        }

        // Entity 생성
        return Resources.builder()
                .name(resource.getName())
                .description(resource.getDescription())
                .resourceImage(resource.getResourceImage())
                .resourceGroup(group)
                .startDate(resource.getStartDate())
                .endDate(resource.getEndDate())
                .timeInterval(resource.getTimeInterval())
                .capacity(resource.getCapacity())
                .row(resource.getRow())
                .col(resource.getCol())
                .status(status)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }


    // 단일 Resource → ResourceDetailInfo 변환
    public ResourceDto.ResourceDetailInfo toResourceDetailInfo(Resources resource) {
        return ResourceDto.ResourceDetailInfo.builder()
                .resourceGroupId(resource.getResourceGroup() != null ? resource.getResourceGroup().getId() : null)
                .resourceGroupName(resource.getResourceGroup() != null ? resource.getResourceGroup().getName() : null)
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .resourceImage(resource.getResourceImage())
                .status(resource.getStatus())
//                .createdBy(resource.getCreatedBy() != null ? resource.getCreatedBy().getName() : null)
                .updatedAt(resource.getUpdatedAt() != null ? resource.getUpdatedAt().format(DATE_FORMATTER) : null)
                .startDate(resource.getStartDate())
                .endDate(resource.getEndDate())
                .timeInterval(resource.getTimeInterval())
                .capacity(resource.getCapacity())
                .row(resource.getRow())
                .col(resource.getCol())
                .category(resource.getResourceGroup() != null ? resource.getResourceGroup().getCategory() : null)
                .isAlwaysAvailable(resource.getResourceGroup() != null ? resource.getResourceGroup().getIsAlwaysAvailable() : null)
                .version(resource.getVersion())
                .build();
    }

    // 리스트 변환
    public List<ResourceDto.ResourceDetailInfo> toResourceDetailInfos(List<Resources> resources) {
        return resources.stream()
                .map(this::toResourceDetailInfo)
                .toList();
    }

    // ResourceListRes 변환
    public ResourceDto.ResourceListRes toResourceListRes(List<Resources> resources) {
        List<ResourceDto.ResourceDetailInfo> infos = toResourceDetailInfos(resources);
        return ResourceDto.ResourceListRes.builder()
                .resources(infos)
                .build();
    }

    // 리소스 수정
    public void updateResourceInfo(Resources resource, ResourceDto.ResourceUpdateReq dto, Long userId) {
        resource.update(dto, userId);
    }

    // 리소스 삭제
    public void softDeleteResource(Resources resource, Long id, List<ResourceTimeSlots> allSlots, List<ResourceTimeSlotExceptions> exceptionSlots) {
        if (!Boolean.TRUE.equals(resource.getIsActive())) {
            throw new IllegalArgumentException("이미 비활성화 또는 삭제된 리소스입니다.");
        }

        // 리소스 상태 변경
        resource.setIsActive(false);
        resource.setUpdatedBy(id);
        resource.softDelete();

        // 연관 정규 시간 슬롯 소프트 삭제
        allSlots.forEach(ResourceTimeSlots::softDelete);

        // 연관 예외 시간 슬롯 소프트 삭제
        exceptionSlots.forEach(ResourceTimeSlotExceptions::softDelete);
    }

    // 리소스 활성화
    public void activateResource(Resources resource, Long id) {
        if (Boolean.TRUE.equals(resource.getIsActive())) {
            throw new IllegalStateException("이미 활성화된 리소스입니다.");
        }

        resource.setIsActive(true);
        resource.setUpdatedBy(id);
    }

    // 리소스 비활성화
    public void deactivateResource(Resources resource, Long id) {
        if (Boolean.FALSE.equals(resource.getIsActive())) {
            throw new IllegalStateException("이미 비활성화된 리소스입니다.");
        }

        resource.setIsActive(false);
        resource.setUpdatedBy(id);
    }

    // 리소스 상태 변경
    public boolean changeStatus(Resources resource, String targetStatusStr, Long updatedBy) {
        ResourceStatus targetStatus = ResourceStatus.valueOf(targetStatusStr);
        if (!isStatusChangeable(resource, targetStatus)) return false;

        resource.setStatus(targetStatus);
        resource.setUpdatedBy(updatedBy);
        return true;
    }

    private boolean isStatusChangeable(Resources resource, ResourceStatus targetStatus){
        ResourceStatus currentStatus = resource.getStatus();
        LocalDate today = LocalDate.now();
        boolean always = resource.getResourceGroup() != null
                && Boolean.TRUE.equals(resource.getResourceGroup().getIsAlwaysAvailable());

        if (always) {
            if (targetStatus == ResourceStatus.PROGRESS_BEFORE) return false;
            return (currentStatus == ResourceStatus.IN_PROGRESS && targetStatus == ResourceStatus.CLOSED)
                    || (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.IN_PROGRESS);
        }

        LocalDate start = resource.getStartDate();
        LocalDate end = resource.getEndDate();

        if (start == null || end == null) return targetStatus == ResourceStatus.CLOSED;
        if (targetStatus == ResourceStatus.CLOSED) return true;
        if (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.IN_PROGRESS)
            return !today.isBefore(start) && !today.isAfter(end);
        if (currentStatus == ResourceStatus.CLOSED && targetStatus == ResourceStatus.PROGRESS_BEFORE)
            return today.isBefore(start);
        if (today.isAfter(end)) return false;

        return false;
    }

    // 리소스 존재 여부 확인
    public Object toExistenceDto(Resources resource) {
        return ResourceDto.ResourceExistenceDto.builder()
                .id(resource.getId())
                .serviceCategory(resource.getResourceGroup() != null
                        ? resource.getResourceGroup().getCategory()
                        : null)
                .startDate(resource.getStartDate())
                .endDate(resource.getEndDate())
                .timeInterval(resource.getTimeInterval())
                .capacity(resource.getCapacity())
                .isActive(Boolean.TRUE.equals(resource.getIsActive()))
                .build();
    }
}
