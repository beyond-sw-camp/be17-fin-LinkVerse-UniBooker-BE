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

        if (targetType == null) {
            // 전체 조회 (USER + RESOURCE)
            result.addAll(
                    userFieldRepository.findByReservationIdAndDeletedAtIsNull(targetId)
                            .stream()
                            .map(CustomFieldDto.CustomFieldValueListRes::fromUserEntity)
                            .toList()
            );

            result.addAll(
                    resourceFieldRepository.findByResourceIdAndDeletedAtIsNull(targetId)
                            .stream()
                            .map(CustomFieldDto.CustomFieldValueListRes::fromResourceEntity)
                            .toList()
            );

        } else if (targetType == CustomTargetType.USER) {
            result = userFieldRepository.findByReservationIdAndDeletedAtIsNull(targetId)
                    .stream()
                    .map(CustomFieldDto.CustomFieldValueListRes::fromUserEntity)
                    .toList();

        } else if (targetType == CustomTargetType.RESOURCE) {
            result = resourceFieldRepository.findByResourceIdAndDeletedAtIsNull(targetId)
                    .stream()
                    .map(CustomFieldDto.CustomFieldValueListRes::fromResourceEntity)
                    .toList();
        }

        return result;
    }
}
