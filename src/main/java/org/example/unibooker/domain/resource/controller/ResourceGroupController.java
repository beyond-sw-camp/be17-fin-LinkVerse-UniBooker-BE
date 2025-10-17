package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.service.ResourceGroupService;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "리소스 그룹 관리", description = "리소스 그룹에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource-group")
public class ResourceGroupController {
    private final ResourceGroupService resourceGroupService;

    // ---------------- 생성 ----------------
    @Operation(summary = "서비스 그룹 생성", description = "예약/신청 서비스 그룹을 생성합니다.")
    @PostMapping
    public BaseResponse register(@RequestBody ResourceGroupDto.ResourceGroupRegisterReq dto) {
        // TODO : 로그인 기능이 개발되면 userId 받아오는 거 수정
        resourceGroupService.register(dto, dto.getUserId());
        return BaseResponse.success("서비스 그룹이 생성되었습니다.");
    }


    // ---------------- 목록 조회 ----------------
    @Operation(summary = "특정 기업의 서비스 그룹 목록 조회", description = "특정 기업의 서비스 그룹 목록을 조회합니다.")
    @GetMapping("company/{companyId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupListRes> getAllResourceGroups(@PathVariable Long companyId) {
        ResourceGroupDto.ResourceGroupListRes response = resourceGroupService.getResourceGroupsByCompanyId(companyId);
        return BaseResponse.success(response);
    }


    // ---------------- 단건 조회 ----------------
    @Operation(summary = "서비스 그룹 상세 조회", description = "특정 서비스 그룹의 이름, 설명, 썸네일 이미지를 조회합니다.")
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupDetailRes> getResourceGroupById(@PathVariable Long resourceGroupId) {
        ResourceGroupDto.ResourceGroupDetailRes response = resourceGroupService.getResourceGroupById(resourceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정용 상세 조회 ----------------
    @Operation(summary = "서비스 그룹 상세 조회(수정용)", description = "특정 서비스 그룹을 생성할 때 입력한 데이터 전체를 조회합니다.")
    @GetMapping("/{resourceGroupId}/edit")
    public BaseResponse<ResourceGroupDto.ResourceGroupUpdateRes> getResourceGroupDetailById(@PathVariable Long resourceGroupId) {
        ResourceGroupDto.ResourceGroupUpdateRes response = resourceGroupService.getResourceGroupUpdateDetail(resourceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "서비스 그룹 수정", description = "기존의 예약/신청 서비스 그룹을 수정합니다.")
    @PutMapping("/{resourceGroupId}")
    public BaseResponse update(@PathVariable Long resourceGroupId,
                       @RequestBody ResourceGroupDto.ResourceGroupUpdateReq dto) {
        resourceGroupService.updateResourceGroup(resourceGroupId, dto, dto.getUserId());
        return BaseResponse.success("서비스 그룹이 수정되었습니다.");
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "서비스 그룹 삭제", description = "기존의 예약/신청 서비스 그룹을 삭제합니다.")
    @DeleteMapping("/{resourceGroupId}")
    public BaseResponse delete(@PathVariable Long resourceGroupId) {
        resourceGroupService.deleteResourceGroup(resourceGroupId);
        return BaseResponse.success("서비스 그룹이 삭제되었습니다.");
    }


    // ---------------- 서비스 그룹 카테고리 & 상시 모집 여부 조회 ----------------
    @Operation(summary = "서비스 그룹의 카테고리 & 상시 모집 여부 조회", description = "서비스 생성에 필요한 필수입력 필드 구성을 위한 데이터를 조회합니다.")
    @GetMapping("/{resourceGroupId}/register")
    public ResourceGroupDto.ServiceRegisterFieldRes getServiceRegisterField(@PathVariable Long resourceGroupId) {
        // TODO: 서비스 그룹 수정 컨트롤러 구현
        return ResourceGroupDto.ServiceRegisterFieldRes.builder().build();
    }


    // ---------------- 서비스 그룹 활성화 상태 변경 ----------------
    @Operation(summary = "서비스 그룹의 활성화 상태를 변경", description = "서비스 그룹의 활성화 상태를 변경합니다.")
    @GetMapping("active/{resourceGroupId}")
    public void ServiceGroupActivationToggle(@PathVariable Long resourceGroupId,
                                        @RequestParam Boolean isActive) {
        // TODO
    }
}
