package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.usecase.port.in.ResourceWebPort;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 리소스(서비스) 관리 컨트롤러
 * - 서비스 생성, 조회, 수정, 삭제
 * - 서비스 활성화/비활성화
 * - 서비스 상태 관리
 */
@Slf4j
@Tag(name = "Resource API", description = "리소스 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource")
public class ResourceWebAdapter {

    /** 리소스 웹 포트 */
    private final ResourceWebPort resourceWebPort;

    /**
     * 서비스 생성
     */
    @Operation(
            summary = "서비스 생성",
            description = "예약/신청 서비스를 생성합니다. (관리자 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping
    public BaseResponse register(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ResourceDto.ResourceRegisterReq dto) {

        log.info("서비스 생성 - userId: {}, groupId: {}", userId, dto.getResourceGroupId());
        resourceWebPort.register(dto, userId);
        return BaseResponse.success("서비스가 생성되었습니다.");
    }

    /**
     * 서비스 목록 조회
     */
    @Operation(
            summary = "서비스 목록 조회",
            description = "특정 서비스 그룹에 속한 서비스 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/group/{serviceGroupId}")
    public BaseResponse<ResourceDto.ResourceListRes> getAllResources(
            @Parameter(description = "서비스 그룹 ID", required = true, example = "1")
            @PathVariable Long serviceGroupId) {

        log.info("서비스 목록 조회 - serviceGroupId: {}", serviceGroupId);
        ResourceDto.ResourceListRes response = resourceWebPort.getAllResourcesByGroupId(serviceGroupId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 상세 조회
     */
    @Operation(
            summary = "서비스 상세 조회",
            description = "특정 서비스의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceId}")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceById(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("서비스 상세 조회 - resourceId: {}", resourceId);
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceById(resourceId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 수정
     */
    @Operation(
            summary = "서비스 수정",
            description = "기존 예약/신청 서비스를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @PutMapping("/{resourceId}")
    public BaseResponse update(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,
            @RequestBody @Valid ResourceDto.ResourceUpdateReq dto) {

        log.info("서비스 수정 - resourceId: {}, userId: {}", resourceId, userId);
        resourceWebPort.update(userId, resourceId, dto);
        return BaseResponse.success("서비스가 수정되었습니다.");
    }

    /**
     * 서비스 삭제
     */
    @Operation(
            summary = "서비스 삭제",
            description = "기존 예약/신청 서비스를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{resourceId}")
    public BaseResponse delete(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("서비스 삭제 - resourceId: {}, userId: {}", resourceId, userId);
        resourceWebPort.deleteResource(resourceId, userId);
        return BaseResponse.success("서비스가 삭제되었습니다.");
    }

    /**
     * 서비스 활성화
     */
    @Operation(
            summary = "서비스 활성화",
            description = "비활성화된 서비스를 활성화합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @GetMapping("/active/{resourceId}")
    public BaseResponse activateResource(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("서비스 활성화 - resourceId: {}, userId: {}", resourceId, userId);
        resourceWebPort.activate(resourceId, userId);
        return BaseResponse.success("서비스가 활성화되었습니다.");
    }

    /**
     * 서비스 비활성화
     */
    @Operation(
            summary = "서비스 비활성화",
            description = "활성화된 서비스를 비활성화합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @GetMapping("/inactive/{resourceId}")
    public BaseResponse deactivateResource(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("서비스 비활성화 - resourceId: {}, userId: {}", resourceId, userId);
        resourceWebPort.deactivate(resourceId, userId);
        return BaseResponse.success("서비스가 비활성화되었습니다.");
    }

    /**
     * 서비스 상태 변경
     */
    @Operation(
            summary = "서비스 상태 변경",
            description = "서비스의 상태를 변경합니다. (ACTIVE/INACTIVE)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
                    @ApiResponse(responseCode = "400", description = "상태 변경 실패"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
            }
    )
    @PatchMapping("/status")
    public BaseResponse changeResourceStatus(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ResourceDto.ResourceStatusChangReq req) {

        log.info("서비스 상태 변경 - resourceId: {}, userId: {}", req.getResourceId(), userId);
        return resourceWebPort.changeStatus(userId, req) ?
                BaseResponse.success("서비스의 상태가 성공적으로 변경되었습니다.")
                : BaseResponse.error(BaseResponseStatus.RESOURCE_STATUS_CHANGE_FAILED);
    }

    // ---------------- 서비스 존재 여부 확인 ----------------
    @Operation(summary = "존재하는 서비스인지 확인", description = "서비스의 존재 여부를 반환합니다.")
    @GetMapping("/{id}/exists")
    public BaseResponse isExistResource(@PathVariable Long resourceId) {
        return BaseResponse.success(resourceWebPort.getResourceIfExists(resourceId));
    }

    // ---------------- 서비스 무작정 조회 ----------------
    @Operation(summary = "서비스 상세 조회 (무작정 조회)", description = "서비스를 무작정 상세 조회합니다.")
    @GetMapping("/all/{resourceId}")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceByIdForSuper(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceByIdForSuper(resourceId);
        return BaseResponse.success(response);
    }

    // ---------------- 서비스 상세 조회 (활성화 & 미삭제 상태 & 비관적 락) ----------------
    @Operation(summary = "서비스 상세 조회(활성화 & 미삭제 상태 & 비관적 락)", description = "서비스를 상세 조회합니다(활성화 & 미삭제 상태 & 비관적 락)")
    @GetMapping("/pessimistic/{resourceId}")
    public BaseResponse<ResourceDto.ResourceDetailInfo> getResourceLock(@PathVariable Long resourceId) {
        ResourceDto.ResourceDetailInfo response = resourceWebPort.getResourceLock(resourceId);
        return BaseResponse.success(response);
    }
}