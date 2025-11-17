package org.example.apiresource.usecase.port.in;

import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;

import java.util.List;

public interface CustomFieldWebPort {

    // 커스텀 필드 생성
    void create(Long resourceGroupId, CustomFieldDto.CustomFieldReq dto);

    // 커스텀 필드 조회
    List<CustomFieldDto.CustomFieldRes> getCustomFields(Long resourceGroupId, CustomTargetType type);

    // 커스텀 필드 수정
    void update(Long customFieldId, CustomFieldDto.CustomFieldReq dto);

    // 커스텀 필드 삭제
    void delete(Long customFieldId);
}