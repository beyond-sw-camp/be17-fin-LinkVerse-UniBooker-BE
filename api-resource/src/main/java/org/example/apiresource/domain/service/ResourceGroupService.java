package org.example.apiresource.domain.service;

import jakarta.persistence.OptimisticLockException;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.CustomFieldSelectDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.model.entity.Resources;
import org.example.common.model.dto.AuthDto;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceGroupService {

    // ResourceGroup 엔티티 생성
    public ResourceGroups createResourceGroup(ResourceGroupDto.ResourceGroupRegisterReq dto, Long userId, Long companyId) {

        return ResourceGroups.builder()
                .name(dto.getName())
                .groupCode(dto.getGroupCode())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .category(dto.getCategory())
                .isAlwaysAvailable(dto.getIsAlwaysAvailable())
                .isActive(true)
                .companyId(companyId)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
    }


    // CustomField 엔티티 생성
    public CustomFieldDefinitions createCustomFieldEntity(CustomFieldDto.CustomFieldReq fieldDto,
                                                          ResourceGroups group) {
        return CustomFieldDefinitions.builder()
                .fieldName(fieldDto.getFieldName())
                .targetType(fieldDto.getTargetType())
                .description(fieldDto.getDescription())
                .dataType(fieldDto.getDataType())
                .isRequired(fieldDto.getRequired())
                .resourceGroup(group)
                .build();
    }


    // 옵션 생성
    public CustomFieldSelectDefinitions createOptionEntity(String optionName, CustomFieldDefinitions field) {
        return CustomFieldSelectDefinitions.builder()
                .name(optionName)
                .customFieldDefinition(field)
                .build();
    }


    // 기업별 리소스 그룹 조회
    public ResourceGroupDto.ResourceGroupListRes toListResponse(List<ResourceGroups> groups) {

        List<ResourceGroupDto.ResourceGroupDetailRes> dtoList = groups.stream()
                .map(group -> {
                    int activeServiceCount = (int) group.getResources().stream()
                            .filter(r -> r.getIsActive() != null && r.getIsActive())
                            .count();

                    return ResourceGroupDto.ResourceGroupDetailRes.builder()
                            .id(group.getId())
                            .name(group.getName())
                            .groupCode(group.getGroupCode())
                            .description(group.getDescription())
                            .category(group.getCategory() != null ? group.getCategory().name() : null)
                            .thumbnail(group.getThumbnail())
                            .createdAt(group.getCreatedAt())
                            .updatedAt(group.getUpdatedAt())
//                            .administrator(group.getCreatedBy() != null ? group.getCreatedBy().getName() : null)
//                            .updatedByName(group.getUpdatedBy() != null ? group.getUpdatedBy().getName() : null)
                            .serviceCount(group.getResources() != null ? group.getResources().size() : 0)
                            .activeServiceCount(activeServiceCount)
                            .serviceCategory(group.getCategory() != null ? group.getCategory().name() : null)
                            .isActive(group.getIsActive() != null ? group.getIsActive() : false)
                            .build();
                })
                .collect(Collectors.toList());

        return ResourceGroupDto.ResourceGroupListRes.builder()
                .resourceGroups(dtoList)
                .build();
    }


    // 리소스 그룹 단일 조회
    public ResourceGroupDto.ResourceGroupDetailRes toResourceGroupInfo(ResourceGroups group) {
        int activeServiceCount = (int) group.getResources().stream()
                .filter(Resources::getIsActive)
                .count();

        return ResourceGroupDto.ResourceGroupDetailRes.builder()
                .id(group.getId())
                .name(group.getName())
                .groupCode(group.getGroupCode())
                .description(group.getDescription())
                .category(group.getCategory() != null ? group.getCategory().name() : null)
                .thumbnail(group.getThumbnail())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
//                .administrator(group.getCreatedBy() != null ? group.getCreatedBy().getName() : null)
//                .updatedByName(group.getUpdatedBy() != null ? group.getUpdatedBy().getName() : null)
                .serviceCount(group.getResources().size())
                .activeServiceCount(activeServiceCount)
                .serviceCategory(group.getCategory().name())
                .isActive(group.getIsActive() != null ? group.getIsActive() : false)
                .build();
    }


    public static ResourceGroupDto.ResourceGroupUpdateRes toResourceGroupUpdateInfo(ResourceGroups group) {
        List<CustomFieldDto.CustomFieldRes> customFields = group.getCustomFieldDefinitions()
                .stream()
                .map(entity -> CustomFieldDto.CustomFieldRes.builder()
                        .id(entity.getId())
                        .fieldName(entity.getFieldName())
                        .dataType(entity.getDataType().name())      // ENUM → 문자열
                        .targetType(entity.getTargetType().name())  // ENUM → 문자열
                        .required(entity.getIsRequired())
                        .description(entity.getDescription())
                        .build())
                .collect(Collectors.toList());

        return ResourceGroupDto.ResourceGroupUpdateRes.builder()
                .name(group.getName())
                .groupCode(group.getGroupCode())  // 프론트 구조에 맞춘 추가사항
                .description(group.getDescription())
                .thumbnail(group.getThumbnail())
                .category(group.getCategory().name())
                .isAlwaysAvailable(group.getIsAlwaysAvailable())
                .customFields(customFields)
                .build();
    }


    // 리소스 그룹 수정
    public void updateResourceGroup(ResourceGroups resourceGroup, Long userId, ResourceGroupDto.ResourceGroupUpdateReq dto) {
        List<CustomFieldDefinitions> newCustomFields = dto.getCustomFields() != null
                ? dto.getCustomFields().stream()
                .map(customFieldReq -> {
                    CustomFieldDefinitions entity = new CustomFieldDefinitions();
                    entity.update(customFieldReq);  // DTO 기반으로 엔티티 필드 설정
                    return entity;
                })
                .toList()
                : Collections.emptyList();

        // 기존 컬렉션 초기화 후 새 필드 추가
        resourceGroup.getCustomFieldDefinitions().clear();
        for (CustomFieldDefinitions field : newCustomFields) {
            field.setResourceGroup(resourceGroup); // 양방향 관계 설정
            resourceGroup.getCustomFieldDefinitions().add(field);
        }

        // 엔티티 업데이트
        resourceGroup.update(
                dto.getName(),
                dto.getGroupCode(),
                dto.getDescription(),
                dto.getThumbnail(),
                dto.getCategory(),
                dto.getIsAlwaysAvailable(),
                userId
        );
    }


    // 리소스 그룹 삭제
    public void deleteResourceGroup(ResourceGroups resourceGroup, Long userId) {
        try {
            if (!Boolean.TRUE.equals(resourceGroup.getIsActive())) {
                throw new IllegalArgumentException("이미 비활성화 또는 삭제된 리소스 그룹입니다.");
            }

            // 상태 변경
            resourceGroup.setIsActive(false);
            resourceGroup.softDelete();
            resourceGroup.setUpdatedBy(userId);

        } catch (OptimisticLockException e) {
            throw new IllegalStateException("다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", e);
        }
    }


    // 리소스 그룹의 정보 반환
    public ResourceGroupDto.ServiceRegisterFieldRes getServiceFields(ResourceGroups resourceGroup) {
        List<CustomFieldDto.CustomFieldRes> customFields = resourceGroup.getCustomFieldDefinitions()
                .stream()
                .map(field -> CustomFieldDto.CustomFieldRes.builder()
                        .id(field.getId())
                        .fieldName(field.getFieldName())
                        .dataType(field.getDataType().name())    // ENUM → 문자열
                        .targetType(field.getTargetType().name()) // ENUM → 문자열
                        .required(field.getIsRequired())
                        .description(field.getDescription())
                        .build())
                .toList();

        return ResourceGroupDto.ServiceRegisterFieldRes.builder()
                .name(resourceGroup.getName())
                .category(resourceGroup.getCategory())
                .isAlwaysAvailable(resourceGroup.getIsAlwaysAvailable())
                .customFields(customFields)
                .build();
    }


    // 리소스 그룹 활성화
    public void activate(ResourceGroups group, Long updatedBy) {
        if (Boolean.TRUE.equals(group.getIsActive())) {
            throw new IllegalArgumentException("이미 활성화된 서비스 그룹입니다.");
        }

        group.setIsActive(true);
        group.setUpdatedBy(updatedBy);
    }


    // 리소스 그룹 비활성화
    public void deactivate(ResourceGroups group, Long updatedBy) {
        if (Boolean.FALSE.equals(group.getIsActive())) {
            throw new IllegalArgumentException("이미 비활성화된 서비스 그룹입니다.");
        }

        group.setIsActive(false);
        group.setUpdatedBy(updatedBy);
    }
}