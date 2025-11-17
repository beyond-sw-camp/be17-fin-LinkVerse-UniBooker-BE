package org.example.apiresource.usecase.port.in;

import org.example.apiresource.domain.model.dto.TimeSlotDto;

import java.util.List;

public interface TimeSlotWebPort {

    // 리소스 예약 가능 시간 조회 (예약 데이터 고려 X)
    List<TimeSlotDto.DailyTimeSlotResponse> getTimeSlotsWithExceptions(Long resourceId, int year, int month, int page, int pageSize);

    // 정규 운영 시간 조회
    List<TimeSlotDto.TimeSlotResponse> getTimeSlots(Long resourceId);

    // 예외 운영 시간 조회
    List<TimeSlotDto.TimeSlotExceptionResponse> getExceptions(Long resourceId);
}