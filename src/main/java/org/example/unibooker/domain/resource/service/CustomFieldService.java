package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.CustomFieldSelectRepository;
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
    private final CustomFieldSelectRepository customFieldSelectRepository;


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
            fields = customFieldRepository.findByResourceGroupAndTargetTypeAndDeletedAtIsNull(group, type);
        } else {
            fields = customFieldRepository.findByResourceGroupAndDeletedAtIsNull(group);
        }

        return fields.stream()
                .map(field -> {
                    CustomFieldDto.CustomFieldRes res = CustomFieldDto.CustomFieldRes.fromEntity(field);

                    // RADIO / CHECKBOX일 경우 옵션 조회
                    if (field.getDataType() == CustomDataType.RADIO
                            || field.getDataType() == CustomDataType.CHECKBOX) {

                        List<String> options = customFieldSelectRepository
                                .findByCustomFieldDefinitionAndDeletedAtIsNull(field)
                                .stream()
                                .map(CustomFieldSelectDefinitions::getName)
                                .toList();

                        res.setOptions(options);
                    }

                    return res;
                })
                .toList();
    }



    // -------------------- 커스텀 필드 수정 --------------------
    public void update(Long customFieldId, CustomFieldDto.CustomFieldReq dto) {
        CustomFieldDefinitions field = customFieldRepository.findByIdAndDeletedAtIsNull(customFieldId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 커스텀 필드입니다."));

        field.update(dto);
    }


    // -------------------- 커스텀 필드 삭제 --------------------
    public void delete(Long customFieldId) {
        // 삭제할 엔티티 조회 (이미 삭제된 건 제외)
        CustomFieldDefinitions field = customFieldRepository.findByIdAndDeletedAtIsNull(customFieldId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 삭제된 커스텀 필드입니다."));

        field.softDelete();
    }
}
