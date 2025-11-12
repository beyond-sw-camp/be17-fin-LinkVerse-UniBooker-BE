package org.example.apiresource.usecase.port.in;

import jakarta.validation.Valid;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.common.user.AuthDto;

public interface ResourceWebPort {

    // 리소스 생성
    void register(ResourceDto.ResourceRegisterReq dto, Long id);

    // 리소스 목록 조회
    ResourceDto.ResourceListRes getAllResourcesByGroupId(Long serviceGroupId);

    // 리소스 단건 조회
    ResourceDto.ResourceDetailInfo getResourceById(Long resourceId);

    // 리소스 수정
    void update(Long id, Long resourceId, ResourceDto.@Valid ResourceUpdateReq dto);

    // 서비스 삭제
    void deleteResource(Long resourceId, Long id);

    // 서비스 활성화
    void activate(Long resourceId, Long id);

    // 서비스 비활성화
    void deactivate(Long resourceId, Long id);

    // 서비스 상태 변경
    boolean changeStatus(AuthDto authUser, ResourceDto.ResourceStatusChangReq req);

    // 서비스 존재 여부 확인
    Object getResourceIfExists(Long resourceId);
}
