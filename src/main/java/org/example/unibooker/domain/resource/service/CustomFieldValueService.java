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
import java.util.concurrent.atomic.AtomicBoolean;

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
        List<Object> savedUserCustomFieldValues = new ArrayList<>();

        for (CustomFieldDto.CustomFieldValue dto : dtos) {
            CustomFieldDefinitions field = customFieldRepository.findByIdAndDeletedAtIsNull(dto.getCustomFieldId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않거나 삭제된 커스텀 필드입니다. fieldId=" + dto.getCustomFieldId()));

            if (field.getTargetType() == CustomTargetType.USER) {
                savedUserCustomFieldValues.add(userFieldRepository.save(dto.toUserEntity(field, targetId)));

            } else if (field.getTargetType() == CustomTargetType.RESOURCE) {
                savedUserCustomFieldValues.add(resourceFieldRepository.save(dto.toResourceEntity(field, targetId)));

            } else {
                throw new IllegalArgumentException("알 수 없는 타겟 타입입니다. fieldId=" + dto.getCustomFieldId());
            }
        }

        return savedUserCustomFieldValues;
    }


    // -------------------- 리소스의 커스텀 필드 값 조회 --------------------
    public List<CustomFieldDto.CustomFieldValueListRes> getResourceFieldValues(Long resourceId) {

        // 존재 여부 검증
        if (!resourceRepository.existsById(resourceId)) {
            throw new IllegalArgumentException("존재하지 않는 리소스입니다. id=" + resourceId);
        }

        // RESOURCE 필드 값 조회
        return resourceFieldRepository.findByResourceIdAndDeletedAtIsNull(resourceId)
                .stream()
                .map(CustomFieldDto.CustomFieldValueListRes::fromResourceEntity)
                .toList();
    }


    // -------------------- 예약의 사용자 커스텀 필드 값 조회 --------------------
    public List<CustomFieldDto.CustomFieldValueListRes> getUserFieldValuesByReservation(Long reservationId) {

        // 예약 존재 여부 검증
        var reservation = reservationRepository.findByIdAndDeletedAtIsNull(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. id=" + reservationId));

        // USER 필드 값 조회
        return userFieldRepository.findByReservationIdAndDeletedAtIsNull(reservation.getId())
                .stream()
                .map(CustomFieldDto.CustomFieldValueListRes::fromUserEntity)
                .toList();
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
