package org.example.apiresource.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.service.CustomFieldService;
import org.example.apiresource.usecase.port.in.CustomFieldWebPort;
import org.example.apiresource.usecase.port.out.CustomFieldDefinitionPersistencePort;
import org.example.apiresource.usecase.port.out.ResourceGroupPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomFieldUseCase implements CustomFieldWebPort {

    private final CustomFieldService customFieldService;
    private final CustomFieldDefinitionPersistencePort customFieldDefinitionPersistencePort;
    private final ResourceGroupPersistencePort resourceGroupPersistencePort;


    // 커스텀 필드 생성
    @Override
    @Transactional
    public void create(Long resourceGroupId, CustomFieldDto.CustomFieldReq dto) {
        ResourceGroups group = resourceGroupPersistencePort.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 서비스 그룹 ID입니다."));

        CustomFieldDefinitions field = customFieldService.toEntity(dto, group);

        customFieldDefinitionPersistencePort.save(field);
    }


    // 커스텀 필드 조회
    @Override
    @Transactional
    public List<CustomFieldDto.CustomFieldRes> getCustomFields(Long resourceGroupId, CustomTargetType type) {
        ResourceGroups group = resourceGroupPersistencePort.findById(resourceGroupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 서비스 그룹 ID입니다."));

        // 커스텀 필드 조회
        List<CustomFieldDefinitions> fields = (type != null) ?
                customFieldDefinitionPersistencePort.findByResourceGroupAndTargetTypeAndDeletedAtIsNull(group, type) :
                customFieldDefinitionPersistencePort.findByResourceGroupAndDeletedAtIsNull(group);


        // 옵션 조회 (RADIO / CHECKBOX)
        Map<Long, List<String>> fieldOptionsMap = fields.stream()
                .filter(f -> f.getDataType() == CustomDataType.RADIO || f.getDataType() == CustomDataType.CHECKBOX)
                .collect(Collectors.toMap(
                        CustomFieldDefinitions::getId,
                        f -> customFieldDefinitionPersistencePort.findSelectOptions(f).stream()
                                .map(CustomFieldSelectDefinitions::getName)
                                .toList()
                ));

        return customFieldService.toCustomFieldRes(fields, fieldOptionsMap);
    }


    // 커스텀 필드 수정
    @Override
    @Transactional
    public void update(Long customFieldId, CustomFieldDto.CustomFieldReq dto) {
        CustomFieldDefinitions field = customFieldDefinitionPersistencePort.findByIdAndDeletedAtIsNull(customFieldId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 커스텀 필드입니다."));

        field.update(dto);
    }


    // 커스텀 필드 삭제
    @Override
    @Transactional
    public void delete(Long customFieldId) {
        CustomFieldDefinitions field = customFieldDefinitionPersistencePort.findByIdAndDeletedAtIsNull(customFieldId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 삭제된 커스텀 필드입니다."));

        field.softDelete();
    }
}