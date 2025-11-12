package org.example.apiresource.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.example.apiresource.domain.model.entity.UserCustomFieldValues;
import org.example.apiresource.domain.service.CustomFieldValueService;
import org.example.apiresource.usecase.port.in.CustomFieldValueWebPort;
import org.example.apiresource.usecase.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomFieldValueUseCase implements CustomFieldValueWebPort {

    private final CustomFieldValueService customFieldValueService;
    private final UserFieldValuePersistencePort userFieldValuePersistencePort;
    private final ResourceFieldValuePersistencePort resourceFieldValuePersistencePort;
    private final CustomFieldDefinitionPersistencePort customFieldDefinitionPersistencePort;
    private final ResourcePersistencePort resourcePersistencePort;
    private final ResourceCustomFieldPersistencePort resourceCustomFieldPersistencePort;


    // 커스텀 필드 값 생성
    @Override
    @Transactional
    public void register(Long targetId, List<CustomFieldDto.CustomFieldValue> dtos) {
        for (CustomFieldDto.CustomFieldValue dto : dtos) {
            CustomFieldDefinitions field = customFieldDefinitionPersistencePort
                    .findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()
                    ));

            List<Object> entities = customFieldValueService.toEntities(targetId, dto, field);

            for (Object entity : entities) {
                if (entity instanceof UserCustomFieldValues userEntity) {
                    userFieldValuePersistencePort.save(userEntity);
                } else if (entity instanceof ResourceCustomFieldValues resourceEntity) {
                    resourceFieldValuePersistencePort.save(resourceEntity);
                }
            }
        }
    }


    // 리소스 커스텀 필드 값 조회
    @Override
    @Transactional
    public List<CustomFieldDto.CustomFieldValueListRes> getResourceFieldValues(Long resourceId) {
        // 리소스 존재 여부 검증
        if (!resourcePersistencePort.existsById(resourceId)) {
            throw new IllegalArgumentException("존재하지 않는 리소스입니다. id=" + resourceId);
        }

        // 모든 RESOURCE 커스텀 필드 값 조회
        List<ResourceCustomFieldValues> fieldValues = resourceCustomFieldPersistencePort
                .findByResourceIdAndDeletedAtIsNull(resourceId);

        return customFieldValueService.toFieldValueList(fieldValues);
    }


    // 예약의 사용자 커스텀 필드 값 조회
//    @Override
//    @Transactional
//    public List<CustomFieldDto.CustomFieldValueListRes> getUserFieldValuesByReservation(Long reservationId) {
//
//        // 예약 존재 여부 검증
//        var reservation = reservationRepository.findById(reservationId)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + reservationId));
//
//        // USER 커스텀 필드 값 조회 (서비스 메서드 호출)
//        List<UserCustomFieldValues> fieldValues = userFieldValuePersistencePort
//                .findByReservationIdAndDeletedAtIsNull(reservation.getId());
//
//        Map<Long, List<String>> groupedValues = customFieldValueService.groupFieldValues(fieldValues);
//        return customFieldValueService.toValueListDto(groupedValues, fieldValues);
//    }


    // 리소스 커스텀 필드 값 수정
    @Override
    @Transactional
    public void update(List<CustomFieldDto.CustomFieldValueUpdateReq> dtoList) {
        for (CustomFieldDto.CustomFieldValueUpdateReq dto : dtoList) {

            // 필드 정의 검증
            var fieldDef = customFieldDefinitionPersistencePort.findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()));

            if (fieldDef.getTargetType() != CustomTargetType.RESOURCE) {
                throw new IllegalArgumentException(
                        "RESOURCE 타입의 커스텀 필드만 수정할 수 있습니다. fieldId=" + dto.getCustomFieldId());
            }

            // 필드 값 엔티티 조회
            ResourceCustomFieldValues entity = resourceFieldValuePersistencePort.findByIdAndDeletedAtIsNull(dto.getCustomFieldValueId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드 값입니다. valueId=" + dto.getCustomFieldValueId()));

            // 필드 일치 검증
            if (!entity.getCustomFieldDefinition().getId().equals(dto.getCustomFieldId())) {
                throw new IllegalArgumentException(
                        "customFieldId가 일치하지 않습니다. valueId=" + dto.getCustomFieldValueId());
            }

            // 값 수정
            entity.updateValue(dto.getValue());
        }
    }


    // 커스텀 필드 값 삭제
    @Override
    @Transactional
    public void delete(List<Long> customFieldValueIds) {
        for (Long valueId : customFieldValueIds) {

            AtomicBoolean deleted = new AtomicBoolean(false);

            // RESOURCE 타입 조회
            resourceFieldValuePersistencePort.findByIdAndDeletedAtIsNull(valueId).ifPresent(entity -> {
                entity.softDelete();
                deleted.set(true);
            });

            // USER 타입 조회
            userFieldValuePersistencePort.findByIdAndDeletedAtIsNull(valueId).ifPresent(entity -> {
                entity.softDelete();
                deleted.set(true);
            });

            if (!deleted.get()) {
                throw new IllegalArgumentException(
                        "존재하지 않거나 이미 삭제된 커스텀 필드 값입니다. valueId=" + valueId);
            }
        }
    }
}
