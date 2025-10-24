package org.example.unibooker.domain.resource.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.ResourceCustomFieldValueRepository;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final ResourceCustomFieldValueRepository resourceCustomFieldValueRepository;


    // -------------------- 리소스 등록 --------------------
    @Transactional
    public void register(ResourceDto.ResourceRegisterReq dto) {
        // TODO : 생성자, 수정자
        dto.validate();

        ResourceGroups group = resourceGroupRepository.findById(dto.getResourceGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        Resources resource = dto.toEntity(group);
//        resource.setTimeInterval(dto.getTimeInterval());
        resourceRepository.save(resource);

        // RESOURCE 커스텀 필드 값 저장
        if (dto.getCustomFieldValues() != null && !dto.getCustomFieldValues().isEmpty()) {
            for (CustomFieldDto.CustomFieldValue customValueDto : dto.getCustomFieldValues()) {
                CustomFieldDefinitions field = customFieldDefinitionRepository.findByIdAndDeletedAtIsNull(customValueDto.getCustomFieldId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + customValueDto.getCustomFieldId()));

                // RESOURCE 타입만 저장
                if (field.getTargetType() == CustomTargetType.RESOURCE) {
                    ResourceCustomFieldValues value = customValueDto.toResourceEntity(field, resource.getId());
                    resourceCustomFieldValueRepository.save(value);
                }
            }
        }
    }


    // -------------------- 리소스 목록 조회 --------------------
    public ResourceDto.ResourceListRes getAllResourcesByGroupId(Long serviceGroupId) {
        List<Resources> resources = resourceRepository
                .findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(serviceGroupId);

        List<ResourceDto.ResourceListInfo> resourceInfos = resources.stream()
                .map(ResourceDto.ResourceListInfo::fromEntity)
                .collect(Collectors.toList());

        return ResourceDto.ResourceListRes.fromEntity(resourceInfos);
    }


    // -------------------- 리소스 상세 조회 (수정용) --------------------
    public ResourceDto.ResourceUpdateRes getResourceDetailForUpdate(Long resourceId) {
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return ResourceDto.ResourceUpdateRes.fromEntity(resource);
    }


    // -------------------- 리소스 상세 조회 (목록 조회용) --------------------
    public ResourceDto.ResourceListInfo getResourceById(Long resourceId) {
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return ResourceDto.ResourceListInfo.fromEntity(resource);
    }


    // -------------------- 리소스 수정 --------------------
    @Transactional
    public void update(Long resourceId, ResourceDto.ResourceUpdateReq dto) {
        dto.validate();

        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스가 존재하지 않습니다."));

        resource.update(dto);
    }


    // -------------------- 리소스 삭제 --------------------
    @Transactional
    public void deleteResource(Long resourceId) {
        try {
            Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스가 존재하지 않습니다."));

            if (!Boolean.TRUE.equals(resource.getIsActive())) {
                throw new IllegalArgumentException("이미 비활성화 또는 삭제된 리소스입니다.");
            }

            resource.setIsActive(false);
            resource.softDelete();

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", e);
        }
    }


    // -------------------- 리소스 활성화 --------------------
    @Transactional
    public void activate(Long resourceId) {

        try {
            // TODO : 플랫폼 관리자 권한을 가졌는지 확인

            Resources resource = resourceRepository.findByIdAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            if (Boolean.TRUE.equals(resource.getIsActive())) {
                log.info("이미 활성화된 서비스입니다. id={}", resourceId);
                return;
            }

            resource.setIsActive(true);
            // resourceGroup.setUpdatedBy(user); // 수정자 추후 추가

            log.info("서비스 활성화 완료 - id={}", resourceId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // -------------------- 리소스 비활성화 --------------------
    @Transactional
    public void deactivate(Long resourceId) {

        try {
            // TODO : 플랫폼 관리자 권한을 가졌는지 확인

            Resources resource = resourceRepository.findByIdAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            if (Boolean.FALSE.equals(resource.getIsActive())) {
                log.info("이미 활성화된 서비스입니다. id={}", resourceId);
                return;
            }

            resource.setIsActive(false);
            // resourceGroup.setUpdatedBy(user); // 수정자 추후 추가

            log.info("서비스 비활성화 완료 - id={}", resourceId);
        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }
}