package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.TimeSlotDto;
import org.example.apiresource.usecase.port.in.TimeSlotWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "타임슬롯 관리", description = "타임슬롯에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeslot")
public class TimeSlotWebAdapter {
    private final TimeSlotWebPort timeSlotWebPort;

    // ---------------- 생성 ----------------
    // 타임슬롯, 예외타임슬롯 생성은 리소스 생성할 때 같이 생성됩니다.


    // ---------------- 조회 ----------------
    @GetMapping("/{resourceId}/timeslots/exceptions")
    public List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlots(
            @PathVariable Long resourceId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "31") int pageSize) {

        return timeSlotWebPort.getTimeSlotsWithExceptions(resourceId, year, month, page, pageSize);
    }


    // ---------------- 정규 운영 시간 조회 ----------------
    @GetMapping("/{resourceId}/timeslots")
    public BaseResponse<List<TimeSlotDto.TimeSlotResponse>> getTimeSlots(@PathVariable Long resourceId) {
        List<TimeSlotDto.TimeSlotResponse> slots = timeSlotWebPort.getTimeSlots(resourceId);

        return BaseResponse.success(slots);
    }


    // ---------------- 예외 운영 시간 조회 ----------------
    @GetMapping("/{resourceId}/exceptions")
    public BaseResponse<List<TimeSlotDto.TimeSlotExceptionResponse>> getResourceExceptions(
            @PathVariable Long resourceId) {
        List<TimeSlotDto.TimeSlotExceptionResponse> result = timeSlotWebPort.getExceptions(resourceId);
        return BaseResponse.success(result);
    }
}
