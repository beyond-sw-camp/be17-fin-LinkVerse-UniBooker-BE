package org.example.apiresource.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.example.apiresource.domain.model.entity.UserCustomFieldValues;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class CustomFieldValueService {

    // 커스텀 필드 값을 User 또는 Resource용 엔티티로 변환
    public List<Object> toEntities(Long targetId, CustomFieldDto.CustomFieldValue dto, CustomFieldDefinitions field) {
        List<Object> entities = new ArrayList<>();

        for (String value : dto.getValues()) {
            switch (field.getTargetType()) {
                case USER -> entities.add(
                        UserCustomFieldValues.builder()
                                .reservationId(targetId)
                                .fieldValue(value)
                                .customFieldDefinition(field)
                                .build()
                );
                case RESOURCE -> entities.add(
                        ResourceCustomFieldValues.builder()
                                .resourceId(targetId)
                                .fieldValue(value)
                                .customFieldDefinition(field)
                                .build()
                );
                default -> throw new IllegalArgumentException(
                        "알 수 없는 타겟 타입입니다. fieldId=" + dto.getCustomFieldId()
                );
            }
        }

        return entities;
    }


    // true/false -> 예/아니오 변환
    public String convertBooleanValue(String v) {
        if ("true".equalsIgnoreCase(v)) return "예";
        if ("false".equalsIgnoreCase(v)) return "아니오";
        return v;
    }


    // 커스텀 필드 값을 customFieldId 기준으로 그룹핑 후 변환
    public List<CustomFieldDto.CustomFieldValueListRes> toFieldValueList(List<ResourceCustomFieldValues> fieldValues) {
        // customFieldId 기준 그룹핑
        Map<Long, List<String>> groupedValues = fieldValues.stream()
                .collect(Collectors.groupingBy(
                        fv -> fv.getCustomFieldDefinition().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(ResourceCustomFieldValues::getFieldValue, Collectors.toList())
                ));

        // DTO 리스트 생성
        List<CustomFieldDto.CustomFieldValueListRes> result = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : groupedValues.entrySet()) {
            CustomFieldDefinitions field = fieldValues.stream()
                    .filter(fv -> fv.getCustomFieldDefinition().getId().equals(entry.getKey()))
                    .findFirst()
                    .get()
                    .getCustomFieldDefinition();

            List<String> convertedValues = entry.getValue().stream()
                    .map(this::convertBooleanValue)
                    .toList();

            result.add(CustomFieldDto.CustomFieldValueListRes.builder()
                    .customFieldId(field.getId())
                    .fieldName(field.getFieldName())
                    .values(convertedValues)
                    .build());
        }
        return result;
    }


    public Map<Long, List<String>> groupFieldValues(List<UserCustomFieldValues> fieldValues) {
        return fieldValues.stream()
                .collect(Collectors.groupingBy(
                        fv -> fv.getCustomFieldDefinition().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(UserCustomFieldValues::getFieldValue, Collectors.toList())
                ));
    }


    public List<CustomFieldDto.CustomFieldValueListRes> toValueListDto(
            Map<Long, List<String>> groupedValues,
            List<UserCustomFieldValues> fieldValues
    ) {
        List<CustomFieldDto.CustomFieldValueListRes> result = new ArrayList<>();

        for (Map.Entry<Long, List<String>> entry : groupedValues.entrySet()) {
            CustomFieldDefinitions field = fieldValues.stream()
                    .filter(fv -> fv.getCustomFieldDefinition().getId().equals(entry.getKey()))
                    .findFirst()
                    .get()
                    .getCustomFieldDefinition();

            result.add(CustomFieldDto.CustomFieldValueListRes.builder()
                    .customFieldId(field.getId())
                    .fieldName(field.getFieldName())
                    .values(entry.getValue())
                    .build());
        }

        return result;
    }
}
