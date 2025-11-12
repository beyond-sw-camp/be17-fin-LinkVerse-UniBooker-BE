package org.example.apiresource.usecase.port.in;

import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.common.user.AuthDto;
import org.example.common.user.UserRole;

public interface ResourceGroupWebPort {

    // 관리자 사용자가 리소스 그룹을 생성
    void register(ResourceGroupDto.ResourceGroupRegisterReq dto, Long userId, Long companyId);

    // 특정 기업의 리소스 그룹 조회
    ResourceGroupDto.ResourceGroupListRes getResourceGroupsByCompanyId(UserRole role, Long companyId);

    // 리소스 그룹 단일 조회
    ResourceGroupDto.ResourceGroupDetailRes getResourceGroupById(AuthDto authUser, Long resourceGroupId);

    // 리소스 그룹 상세 조회(수정용)
    ResourceGroupDto.ResourceGroupUpdateRes getResourceGroupUpdateDetail(Long resourceGroupId);

    // 리소스 그룹 수정
    void updateResourceGroup(AuthDto authDto, Long resourceGroupId, ResourceGroupDto.ResourceGroupUpdateReq resourceGroupDto);

    // 리소스 그룹 삭제
    void deleteResourceGroup(Long id, Long resourceGroupId);

    // 리소스 그룹 카테고리 & 상시 모집 여부 조회
    ResourceGroupDto.ServiceRegisterFieldRes getServiceRegisterField(Long resourceGroupId);

    // 리소스 그룹 활성화
    void activate(AuthDto authUser, Long resourceGroupId);

    // 리소스 그룹 비활성화
    void deactivate(AuthDto authUser, Long resourceGroupId);
}