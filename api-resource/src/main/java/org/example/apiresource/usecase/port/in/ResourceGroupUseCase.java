package org.example.apiresource.usecase.port.in;

public interface ResourceGroupUseCase {

    // 관리자 사용자가 리소스 그룹을 생성
    void register(ResourceGroupDto.resourceGroupRegisterReq dto, Long userId, Long companyId);
}