package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;

import java.util.List;
import java.util.Optional;

public interface CustomFieldDefinitionPersistencePort {

    List<CustomFieldDefinitions> findAllByIdsAndNotDeleted(List<Long> list);

    // 커스텀 필드 생성
    void save(CustomFieldDefinitions field);

    // 타겟 타입을 기반으로 커스텀 필드 조회
    List<CustomFieldDefinitions> findByResourceGroupAndTargetTypeAndDeletedAtIsNull(ResourceGroups group, CustomTargetType type);

    // 타겟 상관없이 전체 모든 커스텀 필드 조회
    List<CustomFieldDefinitions> findByResourceGroupAndDeletedAtIsNull(ResourceGroups group);

    // 커스텀 필드 옵션 조회
   List<CustomFieldSelectDefinitions> findSelectOptions(CustomFieldDefinitions f);

   // 커스텀 필드 존재 여부 확인
    Optional<CustomFieldDefinitions> findByIdAndDeletedAtIsNull(Long customFieldId);
}
