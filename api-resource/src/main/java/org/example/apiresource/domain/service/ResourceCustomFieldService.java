package org.example.apiresource.domain.service;

import lombok.AllArgsConstructor;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.example.apiresource.domain.model.entity.Resources;
import org.example.apiresource.usecase.port.out.CustomFieldDefinitionPersistencePort;
import org.example.apiresource.usecase.port.out.ResourceCustomFieldPersistencePort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ResourceCustomFieldService {

    public List<ResourceCustomFieldValues> toResourceEntities(Resources resource, ResourceDto.ResourceRegisterReq dto, List<CustomFieldDefinitions> fields) {
        List<ResourceCustomFieldValues> allValues = new ArrayList<>();

        for (CustomFieldDto.CustomFieldValue customValueDto : dto.getCustomFieldValues()) {
            // fields에서 해당 field 찾기
            CustomFieldDefinitions field = fields.stream()
                    .filter(f -> f.getId().equals(customValueDto.getCustomFieldId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + customValueDto.getCustomFieldId()));

            if (field.getTargetType() == CustomTargetType.RESOURCE) {
                for (String v : customValueDto.getValues()) {
                    allValues.add(ResourceCustomFieldValues.builder()
                            .resourceId(resource.getId())
                            .fieldValue(v)
                            .customFieldDefinition(field)
                            .build());
                }
            }
        }

        return allValues;
    }
}
