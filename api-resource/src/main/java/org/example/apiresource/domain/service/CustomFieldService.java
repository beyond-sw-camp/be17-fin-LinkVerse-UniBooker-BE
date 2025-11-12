package org.example.apiresource.domain.service;

import lombok.AllArgsConstructor;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CustomFieldService {

    // DTO → Entity 변환
    public CustomFieldDefinitions toEntity(CustomFieldDto.CustomFieldReq dto, ResourceGroups group) {
        CustomFieldDefinitions field = CustomFieldDefinitions.builder()
                .fieldName(dto.getFieldName())
                .description(dto.getDescription())
                .dataType(dto.getDataType())
                .resourceGroup(group)
                .build();
        return field;
    }


    public List<CustomFieldDto.CustomFieldRes> toCustomFieldRes(List<CustomFieldDefinitions> fields, Map<Long, List<String>> fieldOptionsMap) {
        return fields.stream()
                .map(field -> {
                    // 엔티티 → DTO 변환
                    CustomFieldDto.CustomFieldRes res = CustomFieldDto.CustomFieldRes.builder()
                            .id(field.getId())
                            .fieldName(field.getFieldName())
                            .dataType(field.getDataType().name())   // ENUM → 문자열
                            .targetType(field.getTargetType().name()) // ENUM → 문자열
                            .required(field.getIsRequired())
                            .description(field.getDescription())
                            .build();

                    // RADIO / CHECKBOX 옵션 적용
                    if (field.getDataType() == CustomDataType.RADIO
                            || field.getDataType() == CustomDataType.CHECKBOX) {
                        res.setOptions(fieldOptionsMap.getOrDefault(field.getId(), Collections.emptyList()));
                    }

                    return res;
                })
                .toList();
    }
}
