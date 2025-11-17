package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.CustomFieldDefinitionRepository;
import org.example.apiresource.adapter.out.repository.CustomFieldSelectRepository;
import org.example.apiresource.adapter.out.repository.ResourceGroupRepository;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceGroupPersistenceAdapter implements ResourceGroupPersistencePort {

    private final ResourceGroupRepository resourceGroupRepository;
    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final CustomFieldSelectRepository customFieldSelectRepository;

    // 리소스 그룹 저장
    @Override
    @Transactional
    public void saveResourceGroup(ResourceGroups resourceGroup) {
        resourceGroupRepository.save(resourceGroup);
    }

    // 커스텀 필드 저장
    @Override
    @Transactional
    public void saveCustomField(CustomFieldDefinitions customField) {
        customFieldDefinitionRepository.save(customField);
    }

    // 커스텀 필드 옵션 저장
    @Override
    @Transactional
    public void saveOption(CustomFieldSelectDefinitions option) {
        customFieldSelectRepository.save(option);
    }

    // 기업 별 리소스 그룹 조회
    @Override
    @Transactional
    public List<ResourceGroups> findAllByCompanyIdAndRole(Long companyId, boolean isUser) {
        if (isUser) {
            return resourceGroupRepository.findAllByCompanyIdAndIsActive(companyId, true);
        } else {
            return resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);
        }
    }



    // 리소스 그룹 단일 조회
    @Override
    @Transactional
    public Optional<ResourceGroups> findByIdAndNotDeleted(Long id) {
        return resourceGroupRepository.findByIdAndDeletedAtIsNull(id);
    }

    // 리소스 그룹 조회수 증가
    @Override
    @Transactional
    public void incrementViewCount(Long resourceGroupId) {
        resourceGroupRepository.incrementViewCount(resourceGroupId);
    }


    @Override
    @Transactional
    public ResourceGroups findByIdAndDeletedAtIsNull(Long resourceGroupId) {
        return resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리소스 그룹이 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public Optional<ResourceGroups> findById(Long resourceGroupId) {
        return resourceGroupRepository.findById(resourceGroupId);
    }

    @Override
    @Transactional
    public List<ResourceGroups> findAllByCompanyIdAndDeletedAtIsNull(Long companyId) {
        return resourceGroupRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId);
    }

    @Override
    @Transactional
    public List<ResourceGroups> findAllByIsActiveTrueAndDeletedAtIsNull() {
        return resourceGroupRepository.findAllByIsActiveTrueAndDeletedAtIsNull();
    }

    @Override
    @Transactional
    public int countAllByCategoryAndIsActiveTrueAndDeletedAtIsNull(ServiceCategory category) {
        return resourceGroupRepository.countAllByCategoryAndIsActiveTrueAndDeletedAtIsNull(category);
    }
}