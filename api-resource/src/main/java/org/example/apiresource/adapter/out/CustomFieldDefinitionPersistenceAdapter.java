package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.CustomFieldDefinitionRepository;
import org.example.apiresource.adapter.out.repository.CustomFieldSelectRepository;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.usecase.port.out.CustomFieldDefinitionPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomFieldDefinitionPersistenceAdapter implements CustomFieldDefinitionPersistencePort {

    private final CustomFieldDefinitionRepository customFieldDefinitionRepository;
    private final CustomFieldSelectRepository customFieldSelectRepository;

    @Override
    @Transactional
    public List<CustomFieldDefinitions> findAllByIdsAndNotDeleted(List<Long> list) {
        return customFieldDefinitionRepository.findAllByIdInAndDeletedAtIsNull(list);
    }


    // 커스텀 필드 생성
    @Override
    @Transactional
    public void save(CustomFieldDefinitions field) {
        customFieldDefinitionRepository.save(field);
    }


    // 타겟 타입을 기준으로 커스텀 필드 조회
    @Override
    @Transactional
    public List<CustomFieldDefinitions> findByResourceGroupAndTargetTypeAndDeletedAtIsNull(ResourceGroups group, CustomTargetType type) {
        return customFieldDefinitionRepository.findByResourceGroupAndTargetTypeAndDeletedAtIsNull(group, type);
    }


    // 타겟 상관없이 모든 커스텀 필드 조회
    @Override
    @Transactional
    public List<CustomFieldDefinitions> findByResourceGroupAndDeletedAtIsNull(ResourceGroups group) {
        return customFieldDefinitionRepository.findByResourceGroupAndDeletedAtIsNull(group);
    }


    // 커스텀 필드 옵션 조회
    @Override
    @Transactional
    public List<CustomFieldSelectDefinitions> findSelectOptions(CustomFieldDefinitions f) {
        return customFieldSelectRepository.findByCustomFieldDefinitionAndDeletedAtIsNull(f);
    }


    // 커스텀 필드 존재 여부 확인
    @Override
    @Transactional
    public Optional<CustomFieldDefinitions> findByIdAndDeletedAtIsNull(Long customFieldId) {
        return customFieldDefinitionRepository.findByIdAndDeletedAtIsNull(customFieldId);
    }
}
