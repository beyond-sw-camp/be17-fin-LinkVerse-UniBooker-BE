package org.example.apireservation.mapper;

import org.example.apireservation.domain.model.CustomFieldValue;
import org.example.apireservation.usecase.port.out.CustomFieldValueDto;

public class CustomFieldValueMapper {

    public static CustomFieldValueDto toDto(CustomFieldValue domain) {
        return CustomFieldValueDto.builder()
                .customFieldId(domain.getCustomFieldId())
                .fieldName(domain.getFieldName())
                .values(domain.getValues())
                .build();
    }
}
