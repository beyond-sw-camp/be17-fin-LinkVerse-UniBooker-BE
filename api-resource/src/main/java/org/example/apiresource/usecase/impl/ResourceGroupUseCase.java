package org.example.apiresource.usecase.impl;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.service.ResourceGroupService;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.example.common.model.dto.AuthDto;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceGroupUseCase implements ResourceGroupWebPort {
    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupPersistencePort resourceGroupPersistencePort;


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
    public ResourceGroupDto.ResourceGroupDetailRes getResourceGroupById(AuthDto authUser, Long resourceGroupId) {
        // Port Out 통해 DB 조회
        ResourceGroups group = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 서비스 그룹이 존재하지 않습니다."));

        // USER라면 조회수 증가
        if (authUser.getRole() == UserRole.USER) {
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
    public void updateResourceGroup(AuthDto authDto, Long resourceGroupId, ResourceGroupDto.ResourceGroupUpdateReq dto) {
        // Port Out 통해 DB 조회
        ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

        // Domain Service에서 커스텀 필드 처리 및 엔티티 업데이트
        resourceGroupService.updateResourceGroup(resourceGroup, authDto, dto);
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
    public void activate(AuthDto authUser, Long resourceGroupId) {
        try {
            if (authUser.getRole() != UserRole.SUPER) {
                throw new IllegalArgumentException("플랫폼 관리자만 서비스 그룹 상태를 변경할 수 있습니다.");
            }

            ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            resourceGroupService.activate(resourceGroup, authUser.getId());

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }


    // --------------------- 서비스 그룹 비활성화 -----------------------------
    @Override
    public void deactivate(AuthDto authUser, Long resourceGroupId) {
        try {
            if (authUser.getRole() != UserRole.SUPER) {
                throw new IllegalArgumentException("플랫폼 관리자만 서비스 그룹 상태를 변경할 수 있습니다.");
            }

            ResourceGroups resourceGroup = resourceGroupPersistencePort.findByIdAndNotDeleted(resourceGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));

            resourceGroupService.deactivate(resourceGroup, authUser.getId());

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 다시 시도해주세요.");
        }
    }
}
