package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.ResourceCustomFieldValueRepository;
import org.example.unibooker.domain.resource.repository.UserCustomFieldValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldValueService {
    private final CustomFieldDefinitionRepository customFieldRepository;
    private final UserCustomFieldValueRepository userFieldRepository;
    private final ResourceCustomFieldValueRepository resourceFieldRepository;


    // -------------------- 커스텀 필드 값 저장 -------------------
    public void register(List<CustomFieldDto.CustomFieldValue> dtos) {

        for (CustomFieldDto.CustomFieldValue dto : dtos) {
            CustomFieldDefinitions field = customFieldRepository.findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()));

            if (field.getTargetType() == CustomTargetType.USER) {
                userFieldRepository.save(dto.toUserEntity(field));

            } else if (field.getTargetType() == CustomTargetType.RESOURCE) {
                resourceFieldRepository.save(dto.toResourceEntity(field));

            } else {
                throw new IllegalArgumentException("알 수 없는 타겟 타입입니다. fieldId=" + dto.getCustomFieldId());
            }
        }
    }
}
