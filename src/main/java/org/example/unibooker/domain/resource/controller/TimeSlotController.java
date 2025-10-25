package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.TimeSlotDto;
import org.example.unibooker.domain.resource.service.TimeSlotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "타임슬롯 관리", description = "타임슬롯에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeslot")
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    // ---------------- 생성 ----------------
    // 타임슬롯, 예외타임슬롯 생성은 리소스 생성할 때 같이 생성됩니다.


    // ---------------- 조회 ----------------
    @GetMapping("/{resourceId}/timeslots/exceptions")
    public List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlots(
            @PathVariable Long resourceId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int pageSize) {

        return timeSlotService.getTimeSlotsWithExceptions(resourceId, year, month, page, pageSize);
    }
}
