package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.CustomFieldDefinitionRepository;
import org.example.unibooker.domain.resource.repository.ResourceCustomFieldValueRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.domain.resource.repository.UserCustomFieldValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldValueService {
    private final CustomFieldDefinitionRepository customFieldRepository;
    private final UserCustomFieldValueRepository userFieldRepository;
    private final ResourceCustomFieldValueRepository resourceFieldRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;


    // -------------------- 커스텀 필드 값 저장 -------------------
    public List<Object> register(Long targetId, List<CustomFieldDto.CustomFieldValue> dtos) {
        List<Object> savedEntities = new ArrayList<>();

        for (CustomFieldDto.CustomFieldValue dto : dtos) {
            CustomFieldDefinitions field = customFieldRepository.findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()));

            if (field.getTargetType() == CustomTargetType.USER) {
                List<UserCustomFieldValues> entities = dto.toUserEntity(field, targetId); // 여러 엔티티 리스트
                for (UserCustomFieldValues entity : entities) {
                    savedEntities.add(userFieldRepository.save(entity)); // 한 개씩 저장하고 리스트에 추가
                }

            } else if (field.getTargetType() == CustomTargetType.RESOURCE) {
                List<ResourceCustomFieldValues> entities = dto.toResourceEntities(field, targetId);
                for (ResourceCustomFieldValues entity : entities) {
                    savedEntities.add(resourceFieldRepository.save(entity)); // 한 개씩 저장하고 리스트에 추가
                }
            } else {
                throw new IllegalArgumentException(
                        "알 수 없는 타겟 타입입니다. fieldId=" + dto.getCustomFieldId()
                );
            }
        }

        return savedEntities;
    }




    // -------------------- 리소스의 커스텀 필드 값 조회 --------------------
    @Transactional(readOnly = true)
    public List<CustomFieldDto.CustomFieldValueListRes> getResourceFieldValues(Long resourceId) {

        // 리소스 존재 여부 검증
        if (!resourceRepository.existsById(resourceId)) {
            throw new IllegalArgumentException("존재하지 않는 리소스입니다. id=" + resourceId);
        }

        // 모든 RESOURCE 커스텀 필드 값 조회
        List<ResourceCustomFieldValues> fieldValues = resourceFieldRepository
                .findByResourceIdAndDeletedAtIsNull(resourceId);

        // customFieldId 기준으로 그룹핑하여 values 리스트 생성
        Map<Long, List<String>> groupedValues = fieldValues.stream()
                .collect(Collectors.groupingBy(
                        fv -> fv.getCustomFieldDefinition().getId(),
                        LinkedHashMap::new, // 순서 유지
                        Collectors.mapping(ResourceCustomFieldValues::getFieldValue, Collectors.toList())
                ));

        // DTO 생성
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



    // -------------------- 예약의 사용자 커스텀 필드 값 조회 --------------------
    @Transactional(readOnly = true)
    public List<CustomFieldDto.CustomFieldValueListRes> getUserFieldValuesByReservation(Long reservationId) {

        // 예약 존재 여부 검증
        var reservation = reservationRepository.findByIdAndDeletedAtIsNull(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + reservationId));

        // USER 커스텀 필드 값 조회 (서비스 메서드 호출)
        List<UserCustomFieldValues> fieldValues = userFieldRepository
                .findByReservationIdAndDeletedAtIsNull(reservation.getId());

        // customFieldId 기준으로 그룹핑하여 values 리스트 생성
        Map<Long, List<String>> groupedValues = fieldValues.stream()
                .collect(Collectors.groupingBy(
                        fv -> fv.getCustomFieldDefinition().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(UserCustomFieldValues::getFieldValue, Collectors.toList())
                ));

        // DTO 생성
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



    // ---------------- RESOURCE 필드 값 수정 --------------------
    public void update(List<CustomFieldDto.CustomFieldValueUpdateReq> dtoList) {

        for (CustomFieldDto.CustomFieldValueUpdateReq dto : dtoList) {

            // 필드 정의 검증
            var fieldDef = customFieldRepository.findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()));

            if (fieldDef.getTargetType() != CustomTargetType.RESOURCE) {
                throw new IllegalArgumentException(
                        "RESOURCE 타입의 커스텀 필드만 수정할 수 있습니다. fieldId=" + dto.getCustomFieldId());
            }

            // 필드 값 엔티티 조회
            var entity = resourceFieldRepository.findByIdAndDeletedAtIsNull(dto.getCustomFieldValueId())
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


    // ---------------- 커스텀 필드 값 삭제 --------------------
    public void delete(List<Long> valueIds) {

        for (Long valueId : valueIds) {

            AtomicBoolean deleted = new AtomicBoolean(false);

            // RESOURCE 타입 조회
            resourceFieldRepository.findByIdAndDeletedAtIsNull(valueId).ifPresent(entity -> {
                entity.softDelete();
                deleted.set(true);
            });

            // USER 타입 조회
            userFieldRepository.findByIdAndDeletedAtIsNull(valueId).ifPresent(entity -> {
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
