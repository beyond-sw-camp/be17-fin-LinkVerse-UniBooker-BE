package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.CustomFieldDefinitions;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.model.CustomTargetType;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldService {
    private final CustomFieldDefinitionRepository customFieldRepository;
    private final ResourceGroupRepository resourceGroupRepository;


    // -------------------- 커스텀 필드 생성 --------------------
    public void create(Long resourceGroupId, CustomFieldDto.CustomFieldReq dto) {
        ResourceGroups group = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 서비스 그룹 ID입니다."));

        CustomFieldDefinitions field = dto.toEntity();
        field.setResourceGroup(group);

        customFieldRepository.save(field);
    }


    // -------------------- 커스텀 필드 조회 --------------------
    public List<CustomFieldDto.CustomFieldRes> getCustomFields(Long resourceGroupId, CustomTargetType type) {
        // 리소스 그룹 존재 여부 확인
        ResourceGroups group = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 서비스 그룹입니다."));

        List<CustomFieldDefinitions> fields;

        if (type != null) {
            // 특정 targetType만 조회
            fields = customFieldRepository.findByResourceGroupAndTargetTypeAndDeletedAtIsNull(group, type);
        } else {
            // 전체 조회
            fields = customFieldRepository.findByResourceGroupAndDeletedAtIsNull(group);
        }

        return fields.stream()
                .map(CustomFieldDto.CustomFieldRes::fromEntity)
                .toList();
    }
}
