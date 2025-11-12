package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.usecase.port.in.ResourceWebPort;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.user.AuthDto;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리소스 관리", description = "리소스에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceWebAdapter {
    private final ResourceWebPort resourceWebPort;

    // ---------------- 생성 ----------------
    @Operation(summary = "서비스 생성", description = "예약/신청 서비스를 생성합니다.")
    @PostMapping
    public BaseResponse register(@RequestAttribute("authUser") AuthDto authUser,
                                 @RequestBody ResourceDto.ResourceRegisterReq dto) {
        resourceWebPort.register(dto, authUser.getId());
        return BaseResponse.success("서비스가 생성되었습니다.");
    }


    // ---------------- 목록 조회 ----------------
    @Operation(summary = "서비스 목록 조회", description = "서비스 목록을 조회합니다.")
    @GetMapping("/group/{serviceGroupId}")
    public BaseResponse<ResourceDto.ResourceListRes> getAllResources(@PathVariable Long serviceGroupId) {
        ResourceDto.ResourceListRes response = resourceWebPort.getAllResourcesByGroupId(serviceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 단건 조회 ----------------
    @Operation(summary = "서비스 상세 조회", description = "서비스를 상세 조회합니다.")
    @GetMapping("/{resourceId}")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceById(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceById(resourceId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "서비스 수정", description = "기존의 예약/신청 서비스를 수정합니다.")
    @PutMapping("/{resourceId}")
    public BaseResponse update(@RequestAttribute("authUser") AuthDto authUser,
                               @PathVariable Long resourceId,
                               @RequestBody @Valid ResourceDto.ResourceUpdateReq dto) {
        resourceWebPort.update(authUser.getId(), resourceId, dto);
        return BaseResponse.success("서비스가 수정되었습니다.");
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "서비스 삭제", description = "기존의 예약/신청 서비스를 삭제합니다.")
    @DeleteMapping("/{resourceId}")
    public BaseResponse delete(@RequestAttribute("authUser") AuthDto authUser,
                               @PathVariable Long resourceId) {
        resourceWebPort.deleteResource(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 삭제되었습니다.");
    }


    // ---------------- 서비스 활성화 ----------------
    @Operation(summary = "서비스 활성화", description = "비활성화된 서비스를 활성화합니다.")
    @GetMapping("/active/{resourceId}")
    public BaseResponse activateResource(@RequestAttribute("authUser") AuthDto authUser,
                                         @PathVariable Long resourceId) {
        resourceWebPort.activate(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 활성화되었습니다.");
    }


    // ---------------- 서비스 비활성화 ----------------
    @Operation(summary = "서비스 비활성화", description = "활성화된 서비스를 비활성화합니다.")
    @GetMapping("/inactive/{resourceId}")
    public BaseResponse deactivateResource(@RequestAttribute("authUser") AuthDto authUser,
                                           @PathVariable Long resourceId) {
        resourceWebPort.deactivate(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 비활성화되었습니다.");
    }

    // ---------------- 서비스 상태 변경 ----------------
    @Operation(summary = "서비스 상태 변경", description = "서비스의 상태를 변경합니다.")
    @PatchMapping("/status")
    public BaseResponse changeResourceStatus(
            @RequestAttribute("authUser") AuthDto authUser,
            @RequestBody ResourceDto.ResourceStatusChangReq req)
    {
        return resourceWebPort.changeStatus(authUser, req) ?
                BaseResponse.success("서비스의 상태가 성공적으로 변경되었습니다.")
                : BaseResponse.error(BaseResponseStatus.RESOURCE_STATUS_CHANGE_FAILED);
    }

    // ---------------- 서비스 존재 여부 확인 ----------------
    @Operation(summary = "존재하는 서비스인지 확인", description = "서비스의 존재 여부를 반환합니다.")
    @GetMapping("/{id}/exists")
    public BaseResponse isExistResource(@PathVariable Long resourceId) {
        return BaseResponse.success(resourceWebPort.getResourceIfExists(resourceId));
    }

    // ---------------- 서비스 상세 조회 (비활성화,삭제된 리소스도 허용) ----------------
    @Operation(summary = "서비스 상세 조회(비활성화, 삭제 고려X)", description = "서비스를 상세 조회합니다(비활성화, 삭제 고려X)")
    @GetMapping("/{resourceId}/super")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceByIdForSuper(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceByIdForSuper(resourceId);
        return BaseResponse.success(response);
    }

    // ---------------- 서비스 상세 조회 (활성화 & 미삭제 상태 & 비관적 락) ----------------
    @Operation(summary = "서비스 상세 조회(활성화 & 미삭제 상태 & 비관적 락)", description = "서비스를 상세 조회합니다(활성화 & 미삭제 상태 & 비관적 락)")
    @GetMapping("/{resourceId}/super")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceLock(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceLock(resourceId);
        return BaseResponse.success(response);
    }
}
