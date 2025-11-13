package org.example.apiresource.usecase.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.entity.*;
import org.example.apiresource.domain.service.ResourceCustomFieldService;
import org.example.apiresource.domain.service.ResourceService;
import org.example.apiresource.domain.service.ResourceTimeSlotService;
import org.example.apiresource.usecase.port.in.ResourceWebPort;
import org.example.apiresource.usecase.port.out.*;
import org.example.common.model.dto.AuthDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceUseCase implements ResourceWebPort {
    private final ResourceService resourceService;
    private final ResourceTimeSlotService resourceTimeSlotService;
    private final ResourceCustomFieldService resourceCustomFieldService;
    private final ResourcePersistencePort resourcePersistencePort;
    private final ResourceGroupPersistencePort resourceGroupPersistencePort;
    private final CustomFieldDefinitionPersistencePort customFieldDefinitionPersistencePort;
    private final ResourceCustomFieldPersistencePort resourceCustomFieldPersistencePort;
    private final ResourceTimeSlotExceptionPersistencePort resourceTimeSlotExceptionPersistencePort;
    private final ResourceTimeSlotPersistencePort resourceTimeSlotPersistencePort;


    // 리소스 생성
    @Override
    @Transactional
    public void register(ResourceDto.ResourceRegisterReq dto, Long id) {

        // 리소스 검증
        resourceService.validate(dto);

        ResourceGroups group = resourceGroupPersistencePort.findByIdAndNotDeleted(dto.getResourceGroupId())
                .orElseThrow(() -> new IllegalArgumentException("해당 서비스 그룹이 존재하지 않습니다."));

        // 리소스 Entity 생성
        Resources resource = resourceService.createResource(dto, group, id);

        // 리소스 DB 저장
        resourcePersistencePort.createResource(resource);

        // 시간 슬롯/예외 시간/커스텀 필드 저장
        resourceTimeSlotService.generateTimeSlots(group, dto, resource);
        resourceTimeSlotService.generateExceptionSlots(resource, dto);

        // 커스텀 필드: 먼저 필드 정의 조회
        List<CustomFieldDefinitions> fields = customFieldDefinitionPersistencePort.findAllByIdsAndNotDeleted(
                dto.getCustomFieldValues().stream().map(CustomFieldDto.CustomFieldValue::getCustomFieldId).toList()
        );

        // Domain Service에서 엔티티 변환
        List<ResourceCustomFieldValues> entities = resourceCustomFieldService.toResourceEntities(resource, dto, fields);

        // Port를 통해 DB에 저장
        if (!entities.isEmpty()) {
            resourceCustomFieldPersistencePort.saveAll(entities);
        }
    }


    // 리소스 목록 조회
    @Override
    @Transactional
    public ResourceDto.ResourceListRes getAllResourcesByGroupId(Long serviceGroupId) {
        List<Resources> resources = resourcePersistencePort
                .findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(serviceGroupId);

        return resourceService.toResourceListRes(resources);
    }


    // 리소스 단건 조회
    @Override
    @Transactional
    public ResourceDto.ResourceDetailInfo getResourceById(Long resourceId) {
        Resources resource = resourcePersistencePort.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        return resourceService.toResourceDetailInfo(resource);
    }


    // 리소스 수정
    @Override
    @Transactional
    public void update(Long id, Long resourceId, ResourceDto.@Valid ResourceUpdateReq dto) {
        Resources resource = resourcePersistencePort.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

        resourceService.updateResourceInfo(resource, dto, id);
        resourceTimeSlotService.updateSlots(resource, dto);
        resourceTimeSlotService.updateExceptionSlots(resource, dto);

        resourcePersistencePort.saveResource(resource);
        resourceTimeSlotPersistencePort.saveAll(resource.getTimeSlots());
        resourceTimeSlotExceptionPersistencePort.saveAll(resource.getTimeSlotExceptions());
    }


    // 리소스 삭제
    @Override
    @Transactional
    public void deleteResource(Long resourceId, Long id) {
        try {
            Resources resource = resourcePersistencePort.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

            List<ResourceTimeSlots> allSlots = resourceTimeSlotPersistencePort.findByResourceId(resourceId);
            List<ResourceTimeSlotExceptions> exceptionSlots = resourceTimeSlotExceptionPersistencePort.findByResourceId(resourceId);

            resourceService.softDeleteResource(resource, id, allSlots, exceptionSlots);

            resourcePersistencePort.save(resource);
            resourceTimeSlotPersistencePort.saveAll(allSlots);
            resourceTimeSlotExceptionPersistencePort.saveAll(exceptionSlots);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", e);
        }
    }


    // 서비스 활성화
    @Override
    public void activate(Long resourceId, Long id) {
        try {
            Resources resource = resourcePersistencePort.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

            resourceService.activateResource(resource, id);

            resourcePersistencePort.save(resource);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // 서비스 비활성화
    @Override
    public void deactivate(Long resourceId, Long id) {
        try {
            Resources resource = resourcePersistencePort.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리소스입니다."));

            resourceService.deactivateResource(resource, id);

            resourcePersistencePort.save(resource);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // 서비스 상태 변경
    @Override
    @Transactional
    public boolean changeStatus(AuthDto authUser, ResourceDto.ResourceStatusChangReq req) {
        try {
            Resources resource = resourcePersistencePort.findById(req.getResourceId())
                    .orElseThrow(() -> new IllegalArgumentException("리소스를 찾을 수 없습니다."));

            // 낙관적 락 검증
            if (req.getVersion() != null && !resource.getVersion().equals(req.getVersion())) {
                throw new OptimisticLockException("동시 수정 발생");
            }

            // 상태 변경 로직 도메인 서비스에 위임
            boolean changed = resourceService.changeStatus(resource, req.getTargetStatus(), authUser.getId());

            if (changed) resourcePersistencePort.save(resource);

            return changed;

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다.");
        }
    }


    // 서비스 존재 여부 확인
    @Override
    @Transactional
    public Object getResourceIfExists(Long resourceId) {
        Resources resource = resourcePersistencePort
                .findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId)
                .orElse(null);

        if (resource == null) return null;

        return resourceService.toExistenceDto(resource);
    }
}
