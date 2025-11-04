package org.example.unibooker.query.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.query.model.ResourceReservationDto;
import org.example.unibooker.query.service.ResourceReservationQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Schema(name= "ResourceReservation", description = "리소스와 예약 관련 복합 조회")
@RequestMapping("/api/resource-reservation")
public class ResourceReservationQueryController {

    private final ResourceReservationQueryService resourceReservationQueryService;

    /** 하루(시간 슬롯) 단위 */
    @GetMapping("count/day")
    public BaseResponse<List<ResourceReservationDto.ResourceReservationCountRes>> day(
            @RequestParam Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer slotMinutes // default 60
    ) {
        List<ResourceReservationDto.ResourceReservationCountRes> data =
                resourceReservationQueryService.getDayHourlyCounts(groupId, date, slotMinutes);
        return BaseResponse.success(data);
    }

    /** 주(시간 슬롯) 단위 */
    @GetMapping("count/week")
    public BaseResponse<List<ResourceReservationDto.ResourceReservationCountRes>> week(
            @RequestParam Long groupId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) Integer slotMinutes // default 60
    ) {
        List<ResourceReservationDto.ResourceReservationCountRes> data =
                resourceReservationQueryService.getWeekHourlyCounts(groupId, startDate, slotMinutes);
        return BaseResponse.success(data);
    }

    /** 월(하루 슬롯) 단위 */
    @GetMapping("count/month")
    public BaseResponse<List<ResourceReservationDto.ResourceReservationCountRes>> month(
            @RequestParam Long groupId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<ResourceReservationDto.ResourceReservationCountRes> data =
                resourceReservationQueryService.getMonthDailyCounts(groupId, year, month);
        return BaseResponse.success(data);
    }
}
