package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.dto.ResourceGroupDto;
import org.example.apiresource.usecase.port.in.ResourceGroupWebPort;
import org.example.common.base.BaseResponse;
import org.example.common.model.UserRole;
import org.springframework.web.bind.annotation.*;

/**
 * 리소스 그룹(서비스 그룹) 관리 컨트롤러
 * - 서비스 그룹 생성, 조회, 수정, 삭제
 * - 서비스 그룹 활성화/비활성화
 * - 관리자 대시보드 데이터 제공
 */
@Slf4j
@Tag(name = "Resource Group API", description = "리소스 그룹 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resource-group")
public class ResourceGroupWebAdapter {

    /** 리소스 그룹 웹 포트 */
    private final ResourceGroupWebPort resourceGroupWebPort;

    /**
     * 서비스 그룹 생성
     */
    @Operation(
            summary = "서비스 그룹 생성",
            description = "예약/신청 서비스 그룹을 생성합니다. (관리자 권한 필요)",
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

            @Parameter(description = "기업 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-Company-Id") Long companyId,
            @RequestBody ResourceGroupDto.ResourceGroupRegisterReq dto) {

        log.info("서비스 그룹 생성 - userId: {}, companyId: {}", userId, companyId);
        resourceGroupWebPort.register(dto, userId, companyId);
        return BaseResponse.success("서비스 그룹이 생성되었습니다.");
    }

    /**
     * 특정 기업의 서비스 그룹 목록 조회 (슈퍼 관리자용)
     */
    @Operation(
            summary = "특정 기업의 서비스 그룹 목록 조회 (슈퍼 관리자용)",
            description = "슈퍼 관리자가 특정 기업의 서비스 그룹 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/company/{companyId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupListRes> getAllResourceGroupsForSuper(
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") UserRole userRole,

            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId) {

        log.info("서비스 그룹 목록 조회 (슈퍼 관리자) - companyId: {}", companyId);
        ResourceGroupDto.ResourceGroupListRes response =
                resourceGroupWebPort.getResourceGroupsByCompanyId(userRole, companyId);
        return BaseResponse.success(response);
    }

    /**
     * 내 기업의 서비스 그룹 목록 조회
     */
    @Operation(
            summary = "내 기업의 서비스 그룹 목록 조회",
            description = "관리자가 자신의 기업에 속한 서비스 그룹 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/company")
    public BaseResponse<ResourceGroupDto.ResourceGroupListRes> getAllResourceGroups(
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") UserRole userRole,

            @Parameter(description = "기업 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-Company-Id") Long companyId) {

        log.info("서비스 그룹 목록 조회 - companyId: {}", companyId);
        ResourceGroupDto.ResourceGroupListRes response =
                resourceGroupWebPort.getResourceGroupsByCompanyId(userRole, companyId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 그룹 상세 조회
     */
    @Operation(
            summary = "서비스 그룹 상세 조회",
            description = "특정 서비스 그룹의 이름, 설명, 썸네일 이미지를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<ResourceGroupDto.ResourceGroupDetailRes> getResourceGroupById(
            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") UserRole userRole,

            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 그룹 상세 조회 - resourceGroupId: {}", resourceGroupId);
        ResourceGroupDto.ResourceGroupDetailRes response =
                resourceGroupWebPort.getResourceGroupById(userRole, resourceGroupId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 그룹 수정용 상세 조회
     */
    @Operation(
            summary = "서비스 그룹 수정용 상세 조회",
            description = "서비스 그룹 생성 시 입력한 데이터 전체를 조회합니다. (수정 화면용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceGroupId}/edit")
    public BaseResponse<ResourceGroupDto.ResourceGroupUpdateRes> getResourceGroupDetailById(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 그룹 수정용 상세 조회 - resourceGroupId: {}", resourceGroupId);
        ResourceGroupDto.ResourceGroupUpdateRes response =
                resourceGroupWebPort.getResourceGroupUpdateDetail(resourceGroupId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 그룹 수정
     */
    @Operation(
            summary = "서비스 그룹 수정",
            description = "기존 예약/신청 서비스 그룹을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @PutMapping("/{resourceGroupId}")
    public BaseResponse update(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId,
            @RequestBody ResourceGroupDto.ResourceGroupUpdateReq dto) {

        log.info("서비스 그룹 수정 - resourceGroupId: {}, userId: {}", resourceGroupId, userId);
        resourceGroupWebPort.updateResourceGroup(userId, resourceGroupId, dto);
        return BaseResponse.success("서비스 그룹이 수정되었습니다.");
    }

    /**
     * 서비스 그룹 삭제
     */
    @Operation(
            summary = "서비스 그룹 삭제",
            description = "기존 예약/신청 서비스 그룹을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @DeleteMapping("/{resourceGroupId}")
    public BaseResponse delete(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 그룹 삭제 - resourceGroupId: {}, userId: {}", resourceGroupId, userId);
        resourceGroupWebPort.deleteResourceGroup(userId, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 삭제되었습니다.");
    }

    /**
     * 서비스 그룹 카테고리 및 상시 모집 여부 조회
     */
    @Operation(
            summary = "서비스 그룹 카테고리 및 상시 모집 여부 조회",
            description = "서비스 생성 시 필요한 필수 입력 필드 구성을 위한 데이터를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceGroupId}/register")
    public BaseResponse<ResourceGroupDto.ServiceRegisterFieldRes> getServiceRegisterField(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 등록 필드 조회 - resourceGroupId: {}", resourceGroupId);
        ResourceGroupDto.ServiceRegisterFieldRes response =
                resourceGroupWebPort.getServiceRegisterField(resourceGroupId);
        return BaseResponse.success(response);
    }

    /**
     * 서비스 그룹 활성화
     */
    @Operation(
            summary = "서비스 그룹 활성화",
            description = "비활성화된 서비스 그룹을 활성화합니다. (플랫폼 관리자 전용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @PatchMapping("/{resourceGroupId}/activate")
    public BaseResponse activateResourceGroup(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") UserRole userRole,

            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 그룹 활성화 - resourceGroupId: {}, userId: {}", resourceGroupId, userId);
        resourceGroupWebPort.activate(userId, userRole, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 활성화되었습니다.");
    }

    /**
     * 서비스 그룹 비활성화
     */
    @Operation(
            summary = "서비스 그룹 비활성화",
            description = "활성화된 서비스 그룹을 비활성화합니다. (플랫폼 관리자 전용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @PatchMapping("/{resourceGroupId}/deactivate")
    public BaseResponse deactivateResourceGroup(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "사용자 권한 (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Role") UserRole userRole,

            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("서비스 그룹 비활성화 - resourceGroupId: {}, userId: {}", resourceGroupId, userId);
        resourceGroupWebPort.deactivate(userId, userRole, resourceGroupId);
        return BaseResponse.success("서비스 그룹이 비활성화되었습니다.");
    }

    /**
     * 관리자 전체 대시보드 데이터 조회
     */
    @Operation(
            summary = "관리자 전체 대시보드 데이터 조회",
            description = "관리자 전체 대시보드에 필요한 리소스 그룹 관련 데이터를 조회합니다. (내부 통신용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/total-dashboard/{companyId}")
    public ResourceGroupDto.AdminDashboardResourceGroup getAdminTotalDashboard(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId) {

        log.info("관리자 전체 대시보드 데이터 조회 - companyId: {}", companyId);
        return resourceGroupWebPort.getAdminTotalDashboard(companyId);
    }

    /**
     * 플랫폼 관리자 대시보드 데이터 조회
     */
    @Operation(
            summary = "플랫폼 관리자 대시보드 데이터 조회",
            description = "플랫폼 관리자 전체 대시보드에 필요한 서비스 통계 데이터를 조회합니다. (내부 통신용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    @GetMapping("/total-dashboard")
    public ResourceGroupDto.ServiceStatsResponse getSuperTotalDashboard() {
        log.info("플랫폼 관리자 대시보드 데이터 조회");
        return resourceGroupWebPort.getSuperTotalDashboard();
    }

    /**
     * 관리자 그룹별 대시보드 데이터 조회
     */
    @Operation(
            summary = "관리자 그룹별 대시보드 데이터 조회",
            description = "특정 리소스 그룹의 대시보드 데이터를 조회합니다. (내부 통신용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "서비스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/group-dashboard/{resourceGroupId}")
    public ResourceGroupDto.ResourceGroupDashboardResponse getAdminGroupDashboard(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("관리자 그룹별 대시보드 데이터 조회 - resourceGroupId: {}", resourceGroupId);
        return resourceGroupWebPort.getResourceGroupDashboard(resourceGroupId);
    }
}