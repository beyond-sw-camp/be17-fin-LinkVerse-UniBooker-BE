package org.example.apiresource.domain.service;

import org.example.apiresource.domain.model.DayOfWeek;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.ResourceDto;
import org.example.apiresource.domain.model.dto.TimeSlotDto;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.model.entity.ResourceTimeSlotExceptions;
import org.example.apiresource.domain.model.entity.ResourceTimeSlots;
import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class ResourceTimeSlotService {

    public void generateTimeSlots(ResourceGroups group, ResourceDto.ResourceRegisterReq dto, Resources resource) {

        // EVENT 카테고리 제외
        if (group.getCategory() == ServiceCategory.EVENT) return;

        int intervalMinutes = dto.getTimeInterval();
        int slotsPerDay = (24 * 60) / intervalMinutes;

        for (DayOfWeek day : DayOfWeek.values()) {
            for (int i = 0; i < slotsPerDay; i++) {
                LocalTime slotStart = LocalTime.of(0, 0).plusMinutes((long) i * intervalMinutes);
                LocalTime slotEnd = slotStart.plusMinutes(intervalMinutes);

                if (slotEnd.equals(LocalTime.MIDNIGHT)) {
                    slotEnd = LocalTime.of(23, 59, 59);
                }

                boolean active = isActiveSlot(day, slotStart, dto.getTimeSlots());

                ResourceTimeSlots slot = ResourceTimeSlots.builder()
                        .resource(resource)
                        .dayOfWeek(day)
                        .startTime(slotStart)
                        .endTime(slotEnd)
                        .isActive(active)
                        .build();

                resource.addTimeSlot(slot);
            }
        }
    }

    // 각 슬롯이 활성화인지 판단
    private boolean isActiveSlot(DayOfWeek day, LocalTime slotStart, List<TimeSlotDto.TimeSlotRequest> timeSlots) {
        if (timeSlots == null) return false;

        for (TimeSlotDto.TimeSlotRequest slotDto : timeSlots) {
            if (slotDto.getDays() == null) continue;

            for (DayOfWeek dayEnum : slotDto.getDays()) {
                if (dayEnum == day) {
                    LocalTime targetStart = slotDto.getStartTime();
                    LocalTime targetEnd = slotDto.getEndTime();

                    if ((slotStart.equals(targetStart) || slotStart.isAfter(targetStart))
                            && slotStart.isBefore(targetEnd)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 예외 시간 슬롯 생성
    public void generateExceptionSlots(Resources resource, ResourceDto.ResourceRegisterReq dto) {
        if (dto.getExceptionSlots() == null || dto.getExceptionSlots().isEmpty()) return;

        for (TimeSlotDto.ExceptionSlotRequest exDto : dto.getExceptionSlots()) {

            // 휴무가 아닌데 시간 정보가 없으면 예외
            if (!exDto.getIsClosed() && (exDto.getStartTime() == null || exDto.getEndTime() == null)) {
                throw new IllegalArgumentException(
                        "휴무가 아닐 경우 시작시간과 종료시간은 필수입니다. 날짜: " + exDto.getDate());
            }

            // Resources 엔티티에 예외 슬롯 추가
            ResourceTimeSlotExceptions exceptionSlot = ResourceTimeSlotExceptions.builder()
                    .resource(resource)
                    .date(exDto.getDate())
                    .startTime(exDto.getIsClosed() ? null : exDto.getStartTime())
                    .endTime(exDto.getIsClosed() ? null : exDto.getEndTime())
                    .isClosed(exDto.getIsClosed())
                    .note(exDto.getNote())
                    .build();

            resource.addTimeSlotException(exceptionSlot);
        }
    }

    // 시간 슬롯 수정
    public void updateSlots(Resources resource, ResourceDto.ResourceUpdateReq dto) {
        List<ResourceTimeSlots> allSlots = resource.getTimeSlots();
        List<TimeSlotDto.TimeSlotRequest> dtoSlots = dto.getTimeSlots();

        for (ResourceTimeSlots slot : allSlots) {
            boolean active = false;
            if (dtoSlots != null) {
                for (var ts : dtoSlots) {
                    if (ts.getDays().contains(slot.getDayOfWeek())) {
                        if (!slot.getStartTime().isBefore(ts.getStartTime()) &&
                                !slot.getEndTime().isAfter(ts.getEndTime())) {
                            active = true;
                            break;
                        }
                    }
                }
            }
            slot.setIsActive(active);
        }
    }


    // 예외 시간 수정
    public void updateExceptionSlots(Resources resource, ResourceDto.ResourceUpdateReq dto) {
        List<ResourceTimeSlotExceptions> existing = resource.getTimeSlotExceptions();
        existing.forEach(ResourceTimeSlotExceptions::softDelete);

        if (dto.getExceptionSlots() != null) {
            for (var exDto : dto.getExceptionSlots()) {
                ResourceTimeSlotExceptions ex = ResourceTimeSlotExceptions.builder()
                        .resource(resource)
                        .date(exDto.getDate())
                        .startTime(exDto.getIsClosed() ? null : exDto.getStartTime())
                        .endTime(exDto.getIsClosed() ? null : exDto.getEndTime())
                        .isClosed(exDto.getIsClosed())
                        .note(exDto.getNote())
                        .build();
                resource.addTimeSlotException(ex);
            }
        }
    }
}
