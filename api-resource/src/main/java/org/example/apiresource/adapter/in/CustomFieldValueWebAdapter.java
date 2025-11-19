package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.usecase.port.in.CustomFieldValueWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 커스텀 필드 값 관리 컨트롤러
 * - 리소스 및 사용자 커스텀 필드 값 생성, 조회, 수정, 삭제
 * - 예약 기반 커스텀 필드 값 조회
 * - 리소스/사용자별 커스텀 데이터 관리
 */
@Slf4j
@Tag(name = "Custom Field Value API", description = "커스텀 필드 값 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldValueWebAdapter {

    /** 커스텀 필드 값 웹 포트 */
    private final CustomFieldValueWebPort customFieldValueWebPort;

    /**
     * 커스텀 필드 값 생성
     */
    @Operation(
            summary = "커스텀 필드 값 생성",
            description = "특정 대상(리소스/사용자)에 대한 커스텀 필드 값을 생성합니다. 여러 값을 한 번에 저장 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping("/values/{targetId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> registerCustomFieldValues(
            @Parameter(description = "대상 ID (리소스 ID 또는 사용자 ID)", required = true, example = "1")
            @PathVariable Long targetId,
            @RequestBody List<CustomFieldDto.CustomFieldValue> dtos) {

        log.info("커스텀 필드 값 생성 - targetId: {}, count: {}", targetId, dtos.size());
        List<CustomFieldDto.CustomFieldValueListRes> result = customFieldValueWebPort.register(targetId, dtos);
        return BaseResponse.success(result);
    }

    /**
     * 리소스의 커스텀 필드 값 조회
     */
    @Operation(
            summary = "리소스의 커스텀 필드 값 조회",
            description = "특정 리소스에 설정된 커스텀 필드 값을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
            }
    )
    @GetMapping("/value/resource/{resourceId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> getResourceFieldValues(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("리소스 커스텀 필드 값 조회 - resourceId: {}", resourceId);
        List<CustomFieldDto.CustomFieldValueListRes> response = customFieldValueWebPort.getResourceFieldValues(resourceId);
        return BaseResponse.success(response);
    }

    /**
     * 예약 기반 커스텀 필드 값 조회
     */
    @Operation(
            summary = "예약 기반 커스텀 필드 값 조회",
            description = "특정 예약에 입력된 사용자 커스텀 필드 값을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    @GetMapping("/value/reservation/{reservationId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> getUserFieldValuesByReservation(
            @Parameter(description = "예약 ID", required = true, example = "1")
            @PathVariable Long reservationId) {

        log.info("예약 기반 커스텀 필드 값 조회 - reservationId: {}", reservationId);
        List<CustomFieldDto.CustomFieldValueListRes> result = customFieldValueWebPort.getUserFieldValuesByReservation(reservationId);
        return BaseResponse.success(result);
    }

    /**
     * 커스텀 필드 값 수정
     */
    @Operation(
            summary = "커스텀 필드 값 수정",
            description = "리소스의 커스텀 필드 값을 수정합니다. (RESOURCE 전용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드 값을 찾을 수 없음")
            }
    )
    @PutMapping("/value")
    public BaseResponse updateCustomFieldValues(
            @RequestBody List<CustomFieldDto.CustomFieldValueUpdateReq> dtoList) {

        log.info("커스텀 필드 값 수정 - count: {}", dtoList.size());
        customFieldValueWebPort.update(dtoList);
        return BaseResponse.success("커스텀 필드 값이 수정되었습니다.");
    }

    /**
     * 커스텀 필드 값 삭제
     */
    @Operation(
            summary = "커스텀 필드 값 삭제",
            description = "선택한 커스텀 필드 값을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드 값을 찾을 수 없음")
            }
    )
    @DeleteMapping("/value")
    public BaseResponse deleteCustomFieldValues(
            @RequestBody List<Long> customFieldValueIds) {

        log.info("커스텀 필드 값 삭제 - count: {}", customFieldValueIds.size());
        customFieldValueWebPort.delete(customFieldValueIds);
        return BaseResponse.success("커스텀 필드 값이 삭제되었습니다.");
    }
}