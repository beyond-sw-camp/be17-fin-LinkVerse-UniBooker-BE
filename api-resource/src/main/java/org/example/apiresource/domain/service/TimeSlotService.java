package org.example.apiresource.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.dto.TimeSlotDto;
import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<TimeSlotDto.TimeSlotResponse> mergeActiveSlots(List<ResourceTimeSlots> slots) {
        Map<String, List<ResourceTimeSlots>> byDay = slots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.getDayOfWeek().name(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TimeSlotDto.TimeSlotResponse> result = new ArrayList<>();

        for (String day : byDay.keySet()) {
            List<ResourceTimeSlots> daySlots = byDay.get(day);

            LocalTime start = null;
            LocalTime end = null;

            for (int i = 0; i < daySlots.size(); i++) {
                ResourceTimeSlots slot = daySlots.get(i);

                if (slot.getIsActive()) {
                    if (start == null) start = slot.getStartTime();
                    end = slot.getEndTime();
                }

                boolean isLast = (i == daySlots.size() - 1);
                boolean nextInactive = !isLast && !daySlots.get(i + 1).getIsActive();

                // 활성 구간이 끝날 때 DTO 추가
                if ((slot.getIsActive() && (isLast || nextInactive)) && start != null) {
                    result.add(new TimeSlotDto.TimeSlotResponse(
                            day,
                            start != null ? start.format(TIME_FORMATTER) : null,
                            end != null ? end.format(TIME_FORMATTER) : null
                    ));
                    start = null;
                    end = null;
                }
            }
        }

        return result;
    }


    public List<TimeSlotDto.DailyTimeSlotResponse> buildDailyResponses(
            Map<DayOfWeek, List<ResourceTimeSlots>> regularSlots,
            Map<LocalDate, List<ResourceTimeSlotExceptions>> exceptionMap,
            int intervalMinutes,
            LocalDate pageStart,
            LocalDate pageEnd) {

        List<TimeSlotDto.DailyTimeSlotResponse> response = new ArrayList<>();

        for (LocalDate date = pageStart; !date.isAfter(pageEnd); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = mapJavaDayOfWeek(date.getDayOfWeek());
            boolean isClosed = false;
            String note = "";
            List<TimeSlotDto.TimeSlotResponse> slots = new ArrayList<>();

            // 예외 슬롯 처리
            if (exceptionMap.containsKey(date)) {
                for (ResourceTimeSlotExceptions ex : exceptionMap.get(date)) {
                    if (ex.getNote() != null && note.isEmpty()) note = ex.getNote();
                    if (ex.getIsClosed()) {
                        isClosed = true;
                        slots.clear();
                        break;
                    } else {
                        addTimeSlots(slots, ex.getStartTime(), ex.getEndTime(), intervalMinutes, dayOfWeek);
                    }
                }
            } else {
                // 정규 슬롯 처리
                for (ResourceTimeSlots slot : regularSlots.getOrDefault(dayOfWeek, Collections.emptyList())) {
                    addTimeSlots(slots, slot.getStartTime(), slot.getEndTime(), intervalMinutes, dayOfWeek);
                }
            }

            // 시간 정렬 및 DTO 생성
            slots.sort(Comparator.comparing(TimeSlotDto.TimeSlotResponse::getStartTime));
            response.add(TimeSlotDto.DailyTimeSlotResponse.builder()
                    .date(date.toString())
                    .isClosed(isClosed)
                    .note(note)
                    .slots(slots)
                    .build());
        }

        return response;
    }

    private void addTimeSlots(List<TimeSlotDto.TimeSlotResponse> slots,
                              LocalTime start, LocalTime end,
                              int intervalMinutes,
                              DayOfWeek dayOfWeek) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        while (start.isBefore(end)) {
            LocalTime slotEnd = start.plusMinutes(intervalMinutes);
            if (slotEnd.isAfter(end)) slotEnd = end;

            slots.add(TimeSlotDto.TimeSlotResponse.builder()
                    .dayOfWeek(dayOfWeek.name())
                    .startTime(start != null ? start.format(formatter) : null)
                    .endTime(slotEnd != null ? slotEnd.format(formatter) : null)
                    .build());
            start = slotEnd;
        }
    }

    private DayOfWeek mapJavaDayOfWeek(java.time.DayOfWeek javaDay) {
        switch (javaDay) {
            case MONDAY: return DayOfWeek.MON;
            case TUESDAY: return DayOfWeek.TUE;
            case WEDNESDAY: return DayOfWeek.WED;
            case THURSDAY: return DayOfWeek.THU;
            case FRIDAY: return DayOfWeek.FRI;
            case SATURDAY: return DayOfWeek.SAT;
            case SUNDAY: return DayOfWeek.SUN;
            default: throw new IllegalArgumentException("Unknown day: " + javaDay);
        }
    }


    // ResourceTimeSlotExceptions → DTO 변환
    public List<TimeSlotDto.TimeSlotExceptionResponse> toExceptionResponses(List<ResourceTimeSlotExceptions> exceptions) {
        return exceptions.stream()
                .map(ex -> TimeSlotDto.TimeSlotExceptionResponse.builder()
                        .date(ex.getDate().toString())
                        .startTime(ex.getStartTime() != null ? ex.getStartTime().format(TIME_FORMATTER) : null)
                        .endTime(ex.getEndTime() != null ? ex.getEndTime().format(TIME_FORMATTER) : null)
                        .isClosed(ex.getIsClosed())
                        .note(ex.getNote() == null ? "" : ex.getNote())
                        .build()
                )
                .collect(Collectors.toList());
    }
}