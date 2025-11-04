package org.example.unibooker.query.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.query.model.ResourceReservationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ResourceReservationQueryService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 하루 단위(시간 슬롯) – 기본 60분
     */
    @Transactional(readOnly = true)
    public List<ResourceReservationDto.ResourceReservationCountRes> getDayHourlyCounts(Long groupId, LocalDate date, Integer slotMinutes) {
        int step = (slotMinutes == null || slotMinutes <= 0) ? 60 : slotMinutes;

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = dayStart.plusDays(1);

        List<LocalDateTime[]> slots = buildSlots(dayStart, dayEnd, step);

        return countPerResource(groupId, slots);
    }

    /**
     * 주 단위(시간 슬롯) – 기본 60분
     * startDate는 주의 시작일(월요일 등)로 들어오면 좋다.
     */
    @Transactional(readOnly = true)
    public List<ResourceReservationDto.ResourceReservationCountRes> getWeekHourlyCounts(Long groupId, LocalDate startDate, Integer slotMinutes) {
        int step = (slotMinutes == null || slotMinutes <= 0) ? 60 : slotMinutes;

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to   = from.plusDays(7);

        List<LocalDateTime[]> slots = buildSlots(from, to, step); // 시간 슬롯(연속 7일)

        return countPerResource(groupId, slots);
    }

    /**
     * 월 단위(하루 슬롯)
     */
    @Transactional(readOnly = true)
    public List<ResourceReservationDto.ResourceReservationCountRes> getMonthDailyCounts(Long groupId, int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate nextMonthFirst = first.plusMonths(1);

        List<LocalDateTime[]> daySlots = new ArrayList<>();
        for (LocalDate d = first; d.isBefore(nextMonthFirst); d = d.plusDays(1)) {
            LocalDateTime s = d.atStartOfDay();
            LocalDateTime e = s.plusDays(1);
            daySlots.add(new LocalDateTime[]{s, e});
        }

        return countPerResource(groupId, daySlots);
    }

    // ====== 공통 유틸 ======

    /** [from,to) 구간을 slotMinutes 간격으로 쪼갬 */
    private List<LocalDateTime[]> buildSlots(LocalDateTime from, LocalDateTime to, int slotMinutes) {
        List<LocalDateTime[]> slots = new ArrayList<>();
        LocalDateTime cursor = from;
        while (cursor.isBefore(to)) {
            LocalDateTime next = cursor.plusMinutes(slotMinutes);
            if (next.isAfter(to)) next = to;
            slots.add(new LocalDateTime[]{cursor, next});
            cursor = next;
        }
        return slots;
    }

    /** 리소스 목록 × 슬롯 리스트에 대해 count 호출하여 응답 구성 */
    private List<ResourceReservationDto.ResourceReservationCountRes> countPerResource(Long groupId, List<LocalDateTime[]> slots) {
        List<Resources> resources = resourceRepository.findAllByResourceGroupId(groupId);
        List<ResourceReservationDto.ResourceReservationCountRes> result = new ArrayList<>(resources.size());

        for (Resources r : resources) {
            List<ResourceReservationDto.ResourceReservationCountRes.SlotCount> slotCounts = new ArrayList<>(slots.size());
            for (LocalDateTime[] range : slots) {
                int cnt = reservationRepository.countConfirmedByResourceAndRange(r.getId(), range[0], range[1]);
                slotCounts.add(ResourceReservationDto.ResourceReservationCountRes.SlotCount.builder()
                        .startAt(range[0])
                        .endAt(range[1])
                        .count(cnt)
                        .build());
            }
            result.add(ResourceReservationDto.ResourceReservationCountRes.builder()
                    .resourceId(r.getId())
                    .resourceName(r.getName())
                    .slots(slotCounts)
                    .build());
        }
        return result;
    }
}