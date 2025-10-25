package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.TimeSlotDto;
import org.example.unibooker.domain.resource.service.TimeSlotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "타임슬롯 관리", description = "타임슬롯에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeslot")
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    // ---------------- 생성 ----------------
    // 타임슬롯, 예외타임슬롯 생성은 리소스 생성할 때 같이 생성됩니다.


    // ---------------- 단건 조회 ----------------
    @Operation(summary = "타임슬롯 조회", description = "서비스에 대한 타임슬롯을 조회합니다.")
    @GetMapping("/{resourceId}")
    public BaseResponse<List<TimeSlotDto.TimeSlotResponse>> getTimeSlots(@PathVariable Long resourceId) {
        List<TimeSlotDto.TimeSlotResponse> response = timeSlotService.getTimeSlots(resourceId);
        return BaseResponse.success(response);
    }
}
