package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.usecase.port.in.CustomFieldWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 커스텀 필드 정의 관리 컨트롤러
 * - 서비스/사용자 커스텀 필드 정의 생성, 조회, 수정, 삭제
 * - 리소스 그룹별 커스텀 필드 목록 조회
 * - 타입별(SERVICE/USER) 필드 필터링
 */
@Slf4j
@Tag(name = "Custom Field API", description = "커스텀 필드 정의 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldWebAdapter {

    /** 커스텀 필드 웹 포트 */
    private final CustomFieldWebPort customFieldWebPort;

    /**
     * 커스텀 필드 생성
     */
    @Operation(
            summary = "커스텀 필드 생성",
            description = "리소스 그룹에 서비스 또는 사용자 커스텀 필드 정의를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping("/{resourceGroupId}")
    public BaseResponse registerCustomField(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId,
            @RequestBody CustomFieldDto.CustomFieldReq dto) {

        log.info("커스텀 필드 생성 - resourceGroupId: {}, type: {}", resourceGroupId, dto.getTargetType());
        customFieldWebPort.create(resourceGroupId, dto);
        return BaseResponse.success("커스텀 필드가 생성되었습니다.");
    }

    /**
     * 커스텀 필드 목록 조회
     */
    @Operation(
            summary = "커스텀 필드 목록 조회",
            description = "리소스 그룹의 커스텀 필드 정의 목록을 조회합니다. 타입별 필터링 가능(SERVICE/USER).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "리소스 그룹을 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldRes>> getCustomFields(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId,

            @Parameter(description = "타입 필터 (SERVICE/USER/null:전체)", example = "SERVICE")
            @RequestParam(required = false) CustomTargetType type) {

        log.info("커스텀 필드 목록 조회 - resourceGroupId: {}, type: {}", resourceGroupId, type);
        List<CustomFieldDto.CustomFieldRes> fields = customFieldWebPort.getCustomFields(resourceGroupId, type);
        return BaseResponse.success(fields);
    }

    /**
     * 커스텀 필드 수정
     */
    @Operation(
            summary = "커스텀 필드 수정",
            description = "기존 커스텀 필드 정의를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드를 찾을 수 없음")
            }
    )
    @PutMapping("/{customFieldId}")
    public BaseResponse updateCustomField(
            @Parameter(description = "커스텀 필드 ID", required = true, example = "1")
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldReq dto) {

        log.info("커스텀 필드 수정 - customFieldId: {}", customFieldId);
        customFieldWebPort.update(customFieldId, dto);
        return BaseResponse.success("커스텀 필드가 수정되었습니다.");
    }

    /**
     * 커스텀 필드 삭제
     */
    @Operation(
            summary = "커스텀 필드 삭제",
            description = "커스텀 필드 정의를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{customFieldId}")
    public BaseResponse deleteCustomField(
            @Parameter(description = "커스텀 필드 ID", required = true, example = "1")
            @PathVariable Long customFieldId) {

        log.info("커스텀 필드 삭제 - customFieldId: {}", customFieldId);
        customFieldWebPort.delete(customFieldId);
        return BaseResponse.success("커스텀 필드가 삭제되었습니다.");
    }
}