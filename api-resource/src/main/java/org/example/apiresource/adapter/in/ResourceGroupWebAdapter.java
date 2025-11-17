package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.common.base.BaseResponse;
import org.example.common.model.dto.AuthDto;
import org.example.common.model.UserRole;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리소스 그룹 관리", description = "리소스 그룹에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource-group")
public class ResourceGroupWebAdapter {
    private final ResourceGroupWebPort resourceGroupWebPort;

    // ---------------- 생성 ----------------
    @Operation(summary = "서비스 그룹 생성", description = "예약/신청 서비스 그룹을 생성합니다.")
    @PostMapping
    public BaseResponse register(@RequestHeader("X-User-Id") Long userId,
                                 @RequestHeader("X-Company-Id") Long companyId,
                                 @RequestBody ResourceGroupDto.ResourceGroupRegisterReq dto) {

        resourceGroupWebPort.register(dto, userId, companyId);
        return BaseResponse.success("서비스 그룹이 생성되었습니다.");
    }

    // ---------------- 목록 조회(SUPER)----------------
    @Operation(summary = "특정 기업의 서비스 그룹 목록 조회", description = "특정 기업의 서비스 그룹 목록을 조회합니다.")
    @GetMapping("/company/{companyId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupListRes> getAllResourceGroupsForSuper(
            @RequestHeader("X-User-Role") UserRole userRole,
            @PathVariable Long companyId
    ) {
        ResourceGroupDto.ResourceGroupListRes response =
                resourceGroupWebPort.getResourceGroupsByCompanyId(userRole, companyId);
        return BaseResponse.success(response);
    }

    // ---------------- 목록 조회 ----------------
    @Operation(summary = "특정 기업의 서비스 그룹 목록 조회", description = "특정 기업의 서비스 그룹 목록을 조회합니다.")
    @GetMapping("/company")
    public BaseResponse<ResourceGroupDto.ResourceGroupListRes> getAllResourceGroups(@RequestHeader("X-User-Role") UserRole userRole,
                                                                                    @RequestParam("X-Company-Id") Long companyId) {
        ResourceGroupDto.ResourceGroupListRes response = resourceGroupWebPort.getResourceGroupsByCompanyId(userRole, companyId);
        return BaseResponse.success(response);
    }

    // ---------------- 단건 조회 ----------------
    @Operation(summary = "서비스 그룹 상세 조회", description = "특정 서비스 그룹의 이름, 설명, 썸네일 이미지를 조회합니다.")
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupDetailRes> getResourceGroupById(@RequestHeader("X-User-Role") UserRole userRole,
                                                                                      @PathVariable Long resourceGroupId) {
        ResourceGroupDto.ResourceGroupDetailRes response = resourceGroupWebPort.getResourceGroupById(userRole, resourceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정용 상세 조회 ----------------
    @Operation(summary = "서비스 그룹 상세 조회(수정용)", description = "특정 서비스 그룹을 생성할 때 입력한 데이터 전체를 조회합니다.")
    @GetMapping("/{resourceGroupId}/edit")
    public BaseResponse<ResourceGroupDto.ResourceGroupUpdateRes> getResourceGroupDetailById(@PathVariable Long resourceGroupId) {
        ResourceGroupDto.ResourceGroupUpdateRes response = resourceGroupWebPort.getResourceGroupUpdateDetail(resourceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "서비스 그룹 수정", description = "기존의 예약/신청 서비스 그룹을 수정합니다.")
    @PutMapping("/{resourceGroupId}")
    public BaseResponse update(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long resourceGroupId,
                               @RequestBody ResourceGroupDto.ResourceGroupUpdateReq dto) {
        resourceGroupWebPort.updateResourceGroup(userId, resourceGroupId, dto);
        return BaseResponse.success("서비스 그룹이 수정되었습니다.");
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "서비스 그룹 삭제", description = "기존의 예약/신청 서비스 그룹을 삭제합니다.")
    @DeleteMapping("/{resourceGroupId}")
    public BaseResponse delete(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long resourceGroupId) {
        resourceGroupWebPort.deleteResourceGroup(userId, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 삭제되었습니다.");
    }


    // ---------------- 서비스 그룹 카테고리 & 상시 모집 여부 조회 ----------------
    @Operation(summary = "서비스 그룹의 카테고리 & 상시 모집 여부 조회", description = "서비스 생성에 필요한 필수입력 필드 구성을 위한 데이터를 조회합니다.")
    @GetMapping("/{resourceGroupId}/register")
    public BaseResponse<ResourceGroupDto.ServiceRegisterFieldRes> getServiceRegisterField(@PathVariable Long resourceGroupId) {
        ResourceGroupDto.ServiceRegisterFieldRes response = resourceGroupWebPort.getServiceRegisterField(resourceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 서비스 그룹 활성화 ----------------
    @Operation(summary = "서비스 그룹 활성화", description = "비활성화된 서비스 그룹을 활성화합니다. (플랫폼 관리자 전용)")
    @PatchMapping("/{resourceGroupId}/activate")
    public BaseResponse activateResourceGroup(
            @RequestHeader("X-User_Id") Long userId,
            @RequestHeader("X-User-Role") UserRole userRole,
            @PathVariable Long resourceGroupId) {

        resourceGroupWebPort.activate(userId, userRole, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 활성화되었습니다.");
    }


    // ---------------- 서비스 그룹 비활성화 ----------------
    @Operation(summary = "서비스 그룹 비활성화", description = "활성화된 서비스 그룹을 비활성화합니다. (플랫폼 관리자 전용)")
    @PatchMapping("/{resourceGroupId}/deactivate")
    public BaseResponse deactivateResourceGroup(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") UserRole userRole,
            @PathVariable Long resourceGroupId) {

        resourceGroupWebPort.deactivate(userId, userRole, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 비활성화되었습니다.");
    }


    // ---------------- 관리자 대시보드 데이터 조회 ----------------
    @Operation(summary = "관리자 전체 대시보드 데이터 조회", description = "관리자 전체 대시보드에 필요한 데이터를 조회합니다.")
    @GetMapping("/total-dashboard/{companyId}")
    public ResourceGroupDto.AdminDashboardResourceGroup getAdminTotalDashboard(@PathVariable Long companyId) {
        return resourceGroupWebPort.getAdminTotalDashboard(companyId);
    }


    // ---------------- 플랫폼 관리자 대시보드 데이터 조회 ----------------
    @Operation(summary = "관리자 전체 대시보드 데이터 조회", description = "관리자 전체 대시보드에 필요한 데이터를 조회합니다.")
    @GetMapping("/total-dashboard")
    public ResourceGroupDto.ServiceStatsResponse getSuperTotalDashboard() {
        return resourceGroupWebPort.getSuperTotalDashboard();
    }


    // ---------------- 관리자 그룹 대시보드 데이터 조회 ----------------
    @Operation(summary = "관리자 그룹 대시보드 데이터 조회", description = "관리자 전체 대시보드에 필요한 데이터를 조회합니다.")
    @GetMapping("/group-dashboard/{resourceGroupId}")
    public ResourceGroupDto.ResourceGroupDashboardResponse getAdminGroupDashboard(@PathVariable Long resourceGroupId) {
        return resourceGroupWebPort.getResourceGroupDashboard(resourceGroupId);
    }
}