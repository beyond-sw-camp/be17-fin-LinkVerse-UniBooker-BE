package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.ResourceDto;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.service.ResourceService;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리소스 관리", description = "리소스에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceController {
    private final ResourceService resourceService;

    // ---------------- 생성 ----------------
    @Operation(summary = "서비스 생성", description = "예약/신청 서비스를 생성합니다.")
    @PostMapping
    public BaseResponse register(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser,
                                 @RequestBody ResourceDto.ResourceRegisterReq dto) {
        resourceService.register(dto, authUser.getId());
        return BaseResponse.success("서비스가 생성되었습니다.");
    }


    // ---------------- 목록 조회 ----------------
    @Operation(summary = "서비스 목록 조회", description = "서비스 목록을 조회합니다.")
    @GetMapping("/group/{serviceGroupId}")
    public BaseResponse<ResourceDto.ResourceListRes> getAllResources(@PathVariable Long serviceGroupId) {
        ResourceDto.ResourceListRes response = resourceService.getAllResourcesByGroupId(serviceGroupId);
        return BaseResponse.success(response);
    }


    // ---------------- 단건 조회 ----------------
    @Operation(summary = "서비스 상세 조회", description = "서비스를 상세 조회합니다.")
    @GetMapping("/{resourceId}")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceById(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceService.getResourceById(resourceId);
        return BaseResponse.success(response);
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "서비스 수정", description = "기존의 예약/신청 서비스를 수정합니다.")
    @PutMapping("/{resourceId}")
    public BaseResponse update(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser,
                               @PathVariable Long resourceId,
                               @RequestBody @Valid ResourceDto.ResourceUpdateReq dto) {
        resourceService.update(authUser.getId(), resourceId, dto);
        return BaseResponse.success("서비스가 수정되었습니다.");
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "서비스 삭제", description = "기존의 예약/신청 서비스를 삭제합니다.")
    @DeleteMapping("/{resourceId}")
    public BaseResponse delete(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser,
                               @PathVariable Long resourceId) {
        resourceService.deleteResource(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 삭제되었습니다.");
    }


    // ---------------- 서비스 활성화 ----------------
    @Operation(summary = "서비스 활성화", description = "비활성화된 서비스를 활성화합니다.")
    @GetMapping("/active/{resourceId}")
    public BaseResponse activateResource(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser,
                                         @PathVariable Long resourceId) {
        resourceService.activate(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 활성화되었습니다.");
    }


    // ---------------- 서비스 비활성화 ----------------
    @Operation(summary = "서비스 비활성화", description = "활성화된 서비스를 비활성화합니다.")
    @GetMapping("/inactive/{resourceId}")
    public BaseResponse deactivateResource(@AuthenticationPrincipal AuthDto.AuthenticatedUser authUser,
                                           @PathVariable Long resourceId) {
        resourceService.deactivate(resourceId, authUser.getId());
        return BaseResponse.success("서비스가 비활성화되었습니다.");
    }
}
