package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.DayOfWeek;
import org.example.unibooker.domain.resource.model.ResourceTimeSlotExceptions;
import org.example.unibooker.domain.resource.model.ResourceTimeSlots;
import org.example.unibooker.domain.resource.model.TimeSlotDto;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.domain.resource.repository.ResourceTimeSlotExceptionRepository;
import org.example.unibooker.domain.resource.repository.ResourceTimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final ResourceTimeSlotRepository resourceTimeSlotRepository;
    private final ResourceTimeSlotExceptionRepository resourceTimeSlotExceptionRepository;
    private final ResourceRepository resourceRepository;


    // -------------------- 리소스 상세 조회용 --------------------
    public List<TimeSlotDto.TimeSlotResponse> getTimeSlots(Long resourceId) {
        List<ResourceTimeSlots> slots = resourceTimeSlotRepository.findByResourcesIdOrderByDayOfWeekAscStartTimeAsc(resourceId);

        // 요일별로 그룹화
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

                // 현재 활성 구간 끝나면 DTO 추가
                if ((slot.getIsActive() && (isLast || nextInactive)) && start != null) {
                    result.add(new TimeSlotDto.TimeSlotResponse(
                            day,
                            start,
                            end
                    ));
                    start = null;
                    end = null;
                }
            }
        }

        return result;
    }


    // -------------------- 특정 리소스의 예외 운영 시간 조회 --------------------
    public List<TimeSlotDto.TimeSlotExceptionResponse> getExceptions(Long resourceId) {
        List<ResourceTimeSlotExceptions> exceptions = resourceTimeSlotExceptionRepository
                .findByResources_IdOrderByDateAscStartTimeAsc(resourceId);

        return exceptions.stream()
                .map(TimeSlotDto.TimeSlotExceptionResponse::fromEntity)
                .collect(Collectors.toList());
    }


    // -------------------- 특정 리소스의 운영 시간 조회 --------------------
    // 예외 시간 적용한 운영시간입니다!! 따로 예외시간 처리할 필요 없어용
    @Transactional
    public List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlotsWithExceptions(
            Long resourceId, int year, int month, int page, int pageSize) {

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // 페이지 범위 계산 (0-based)
        LocalDate pageStart = monthStart.plusDays((long) page * pageSize);
        LocalDate pageEnd = pageStart.plusDays(pageSize - 1);
        if (pageEnd.isAfter(monthEnd)) pageEnd = monthEnd;

        // 리소스 단위 시간 간격 조회
        int intervalMinutes = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"))
                .getTimeInterval();

        // 정규 시간 슬롯 전체 조회 (DayOfWeek 기준)
        Map<DayOfWeek, List<ResourceTimeSlots>> regularSlots = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            regularSlots.put(day, resourceTimeSlotRepository.findByResources_IdAndDayOfWeekAndIsActiveTrue(resourceId, day));
        }

        // 예외 시간 슬롯 조회 (deletedAt null 체크)
        List<ResourceTimeSlotExceptions> exceptions =
                resourceTimeSlotExceptionRepository.findByResources_IdAndDateBetweenAndDeletedAtIsNull(resourceId, pageStart, pageEnd);
        Map<LocalDate, List<ResourceTimeSlotExceptions>> exceptionMap =
                exceptions.stream().collect(Collectors.groupingBy(ResourceTimeSlotExceptions::getDate));

        List<TimeSlotDto.DailyTimeSlotResponse> response = new ArrayList<>();

        for (LocalDate date = pageStart; !date.isAfter(pageEnd); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = mapJavaDayOfWeek(date.getDayOfWeek());
            boolean isClosed = false;
            String note = "";
            List<TimeSlotDto.TimeSlotResponse> slots = new ArrayList<>();

            // -------------------- 예외 슬롯 처리 --------------------
            if (exceptionMap.containsKey(date)) {
                for (ResourceTimeSlotExceptions ex : exceptionMap.get(date)) {
                    if (ex.getNote() != null && note.isEmpty()) note = ex.getNote();
                    if (ex.getIsClosed()) {
                        isClosed = true;
                        slots.clear();
                        break;
                    } else {
                        // 예외 시간 슬롯을 interval 단위로 쪼개서 추가
                        LocalTime start = ex.getStartTime();
                        LocalTime end = ex.getEndTime();
                        while (start.isBefore(end)) {
                            LocalTime slotEnd = start.plusMinutes(intervalMinutes);
                            if (slotEnd.isAfter(end)) slotEnd = end;
                            slots.add(TimeSlotDto.TimeSlotResponse.fromEntity(
                                    ResourceTimeSlots.builder()
                                            .dayOfWeek(dayOfWeek)
                                            .startTime(start)
                                            .endTime(slotEnd)
                                            .build()
                            ));
                            start = slotEnd;
                        }
                    }
                }
            } else {
                // -------------------- 정규 슬롯 적용 --------------------
                List<ResourceTimeSlots> dailySlots = regularSlots.getOrDefault(dayOfWeek, Collections.emptyList());
                for (ResourceTimeSlots slot : dailySlots) {
                    LocalTime start = slot.getStartTime();
                    LocalTime end = slot.getEndTime();
                    while (start.isBefore(end)) {
                        LocalTime slotEnd = start.plusMinutes(intervalMinutes);
                        if (slotEnd.isAfter(end)) slotEnd = end;
                        slots.add(TimeSlotDto.TimeSlotResponse.fromEntity(
                                ResourceTimeSlots.builder()
                                        .dayOfWeek(dayOfWeek)
                                        .startTime(start)
                                        .endTime(slotEnd)
                                        .build()
                        ));
                        start = slotEnd;
                    }
                }
            }

            // 시간 기준 정렬
            slots.sort(Comparator.comparing(TimeSlotDto.TimeSlotResponse::getStartTime));

            // DailyTimeSlotResponse 생성
            response.add(TimeSlotDto.DailyTimeSlotResponse.fromEntity(date, isClosed, note, slots));
        }

        return response;
    }


    // DayOfWeek 매핑
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




}
