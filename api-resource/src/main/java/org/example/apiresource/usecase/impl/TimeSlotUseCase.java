package org.example.apiresource.usecase.impl;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.dto.TimeSlotDto;
import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.example.apiresource.domain.service.TimeSlotService;
import org.example.apiresource.usecase.port.in.TimeSlotWebPort;
import org.example.apiresource.usecase.port.out.ResourcePersistencePort;
import org.example.apiresource.usecase.port.out.ResourceTimeSlotExceptionPersistencePort;
import org.example.apiresource.usecase.port.out.ResourceTimeSlotPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotUseCase implements TimeSlotWebPort {

    private final TimeSlotService timeSlotService;
    private final ResourceTimeSlotPersistencePort resourceTimeSlotPersistencePort;
    private final ResourcePersistencePort resourcePersistencePort;
    private final ResourceTimeSlotExceptionPersistencePort resourceTimeSlotExceptionPersistencePort;


    // 예약 가능한 시간 조회
    @Override
    @Transactional
    public List<TimeSlotDto.TimeSlotResponse> getTimeSlots(Long resourceId) {
        // 타임슬롯 조회
        List<ResourceTimeSlots> slots =
                resourceTimeSlotPersistencePort.findByResourceIdOrderByDayOfWeekAscStartTimeAsc(resourceId);

        return timeSlotService.mergeActiveSlots(slots);
    }


    // 정규 운영 시간 조회

    @Override
    @Transactional
    public List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlotsWithExceptions(
            Long resourceId, int year, int month, int page, int pageSize) {

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDate pageStart = monthStart.plusDays((long) page * pageSize);
        LocalDate pageEnd = pageStart.plusDays(pageSize - 1);
        if (pageEnd.isAfter(monthEnd)) pageEnd = monthEnd;

        // DB 접근 → Port 호출
        int intervalMinutes = resourcePersistencePort.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"))
                .getTimeInterval();

        // 정규 슬롯 로드
        Map<DayOfWeek, List<ResourceTimeSlots>> regularSlots = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            regularSlots.put(day, resourceTimeSlotPersistencePort.findByResource_IdAndDayOfWeekAndIsActiveTrue(resourceId, day));
        }

        // 예외 슬롯 로드
        Map<LocalDate, List<ResourceTimeSlotExceptions>> exceptionMap =
                resourceTimeSlotExceptionPersistencePort.findByResource_IdAndDateBetweenAndDeletedAtIsNull(resourceId, pageStart, pageEnd)
                        .stream()
                        .collect(Collectors.groupingBy(ResourceTimeSlotExceptions::getDate));

        // 계산 및 DTO 생성은 service에 위임
        return timeSlotService.buildDailyResponses(
                regularSlots, exceptionMap, intervalMinutes, pageStart, pageEnd
        );
    }


    // 예외 운영 시간 조
    @Override
    @Transactional
    public List<TimeSlotDto.TimeSlotExceptionResponse> getExceptions(Long resourceId) {
        List<ResourceTimeSlotExceptions> exceptions = resourceTimeSlotExceptionPersistencePort
                .findByResource_IdOrderByDateAscStartTimeAsc(resourceId);

        return timeSlotService.toExceptionResponses(exceptions);
    }
}