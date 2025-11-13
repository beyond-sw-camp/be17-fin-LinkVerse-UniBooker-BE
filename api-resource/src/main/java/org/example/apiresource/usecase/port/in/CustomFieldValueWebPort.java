package org.example.apiresource.usecase.port.in;

import org.example.apiresource.domain.model.dto.CustomFieldDto;

import java.util.List;

public interface CustomFieldValueWebPort {

    // 커스텀 필드 값 저장
    List<CustomFieldDto.CustomFieldValueListRes> register(Long targetId, List<CustomFieldDto.CustomFieldValue> dtos);

    // 리소스 커스텀 필드 값 조회
    List<CustomFieldDto.CustomFieldValueListRes> getResourceFieldValues(Long resourceId);

    // 예약의 사용자 커스텀 필드 값 조회
    List<CustomFieldDto.CustomFieldValueListRes> getUserFieldValuesByReservation(Long reservationId);

    // 리소스 커스텀 필드 값 수정
    void update(List<CustomFieldDto.CustomFieldValueUpdateReq> dtoList);

    // 커스텀 필드 값 삭제
    void delete(List<Long> customFieldValueIds);
}
