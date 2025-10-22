package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
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


    // -------------------- 커스텀 필드 값 조회 -------------------
    public List<CustomFieldDto.CustomFieldValueListRes> getCustomFieldValues(Long targetId, CustomTargetType targetType) {

        // TODO : 예약 ID 존재 여부 검증하는 서비스 코드가 생기면 레포지토리를 서비스로 변경 후 주석 해제
        // targetId 존재 여부 검증
//        boolean exists;
//        if (targetType == null) {
//            // 전체 조회 시 — 두 테이블 중 하나라도 존재해야 함
//            exists = resourceRepository.existsById(targetId) || reservationRepository.existsById(targetId);
//        } else if (targetType == CustomTargetType.RESOURCE) {
//            exists = resourceRepository.existsById(targetId);
//        } else if (targetType == CustomTargetType.USER) {
//            exists = reservationRepository.existsById(targetId);
//        } else {
//            throw new IllegalArgumentException("유효하지 않은 targetType입니다.");
//        }
//
//        if (!exists) {
//            throw new IllegalArgumentException("존재하지 않는 targetId입니다. id=" + targetId);
//        }

        List<CustomFieldDto.CustomFieldValueListRes> result = new ArrayList<>();

        // targetType이 null일 경우 → USER + RESOURCE 모두 조회
        if (targetType == null) {
            // USER 필드값: 해당 리소스에 연결된 예약들의 값
//            List<Long> reservationIds = reservationRepository.findIdsByResourceId(targetId);
//            result.addAll(
//                    userFieldRepository.findByReservationIdInAndDeletedAtIsNull(reservationIds)
//                            .stream()
//                            .map(CustomFieldDto.CustomFieldValueListRes::fromUserEntity)
//                            .toList()
//            );

            // RESOURCE 필드값
            result.addAll(
                    resourceFieldRepository.findByResourceIdAndDeletedAtIsNull(targetId)
                            .stream()
                            .map(CustomFieldDto.CustomFieldValueListRes::fromResourceEntity)
                            .toList()
            );

            return result;
        }

        // USER 전용 조회
//        if (targetType == CustomTargetType.USER) {
//            List<Long> reservationIds = reservationRepository.findIdsByResourceId(targetId);
//
//            return userFieldRepository.findByReservationIdInAndDeletedAtIsNull(reservationIds)
//                    .stream()
//                    .map(CustomFieldDto.CustomFieldValueListRes::fromUserEntity)
//                    .toList();
//        }

        // RESOURCE 전용 조회
        if (targetType == CustomTargetType.RESOURCE) {
            return resourceFieldRepository.findByResourceIdAndDeletedAtIsNull(targetId)
                    .stream()
                    .map(CustomFieldDto.CustomFieldValueListRes::fromResourceEntity)
                    .toList();
        }

        throw new IllegalArgumentException("유효하지 않은 targetType입니다.");
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
