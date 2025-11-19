package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.dto.TimeSlotDto;
import org.example.apiresource.usecase.port.in.TimeSlotWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 타임슬롯 관리 컨트롤러
 * - 정규 운영 시간 조회
 * - 예외 운영 시간 조회
 * - 월별 타임슬롯 조회 (예외 포함)
 */
@Slf4j
@Tag(name = "TimeSlot API", description = "타임슬롯 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeslot")
public class TimeSlotWebAdapter {

    /** 타임슬롯 웹 포트 */
    private final TimeSlotWebPort timeSlotWebPort;

    /**
     * 월별 타임슬롯 조회 (예외 포함)
     */
    @Operation(
            summary = "월별 타임슬롯 조회 (예외 포함)",
            description = "특정 리소스의 월별 타임슬롯을 정규 운영 시간과 예외 운영 시간을 포함하여 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceId}/timeslots/exceptions")
    public List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlots(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "조회 연도", required = true, example = "2025")
            @RequestParam int year,

            @Parameter(description = "조회 월", required = true, example = "1")
            @RequestParam int month,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "31")
            @RequestParam(defaultValue = "31") int pageSize) {

        log.info("월별 타임슬롯 조회 - resourceId: {}, year: {}, month: {}", resourceId, year, month);
        return timeSlotWebPort.getTimeSlotsWithExceptions(resourceId, year, month, page, pageSize);
    }

    /**
     * 정규 운영 시간 조회
     */
    @Operation(
            summary = "정규 운영 시간 조회",
            description = "특정 리소스의 정규 운영 시간(요일별 타임슬롯)을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceId}/timeslots")
    public BaseResponse<List<TimeSlotDto.TimeSlotResponse>> getTimeSlots(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("정규 운영 시간 조회 - resourceId: {}", resourceId);
        List<TimeSlotDto.TimeSlotResponse> slots = timeSlotWebPort.getTimeSlots(resourceId);
        return BaseResponse.success(slots);
    }

    /**
     * 예외 운영 시간 조회
     */
    @Operation(
            summary = "예외 운영 시간 조회",
            description = "특정 리소스의 예외 운영 시간(특정 날짜별 타임슬롯)을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceId}/exceptions")
    public BaseResponse<List<TimeSlotDto.TimeSlotExceptionResponse>> getResourceExceptions(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("예외 운영 시간 조회 - resourceId: {}", resourceId);
        List<TimeSlotDto.TimeSlotExceptionResponse> result = timeSlotWebPort.getExceptions(resourceId);
        return BaseResponse.success(result);
    }
}