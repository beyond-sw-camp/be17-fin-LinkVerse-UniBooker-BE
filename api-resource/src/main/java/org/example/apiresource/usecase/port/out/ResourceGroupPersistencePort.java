package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;

import java.util.List;
import java.util.Optional;


public interface ResourceGroupPersistencePort {

    // 리소스 그룹 저장
    void saveResourceGroup(ResourceGroups resourceGroup);

    // 커스텀 필드 저장
    void saveCustomField(CustomFieldDefinitions customField);

    // 커스텀 필드 옵션 저장
    void saveOption(CustomFieldSelectDefinitions option);

    // 기업 별 리소스 그룹 조회
    List<ResourceGroups> findAllByCompanyIdAndRole(Long companyId, boolean isUser);

    // 삭제되지 않은 리소스 그룹 단일 조회
    Optional<ResourceGroups> findByIdAndNotDeleted(Long id);

    // 리소스 그룹 조회수 증가
    void incrementViewCount(Long resourceGroupId);

    ResourceGroups findByIdAndDeletedAtIsNull(Long resourceGroupId);

    Optional<ResourceGroups> findById(Long resourceGroupId);

    List<ResourceGroups> findAllByCompanyIdAndDeletedAtIsNull(Long companyId);

    List<ResourceGroups> findAllByIsActiveTrueAndDeletedAtIsNull();

    int countAllByCategoryAndIsActiveTrueAndDeletedAtIsNull(ServiceCategory category);
}
