package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "리소스 타임슬롯 정보 DTO")
public class TimeSlotDto {

    @Getter
    @Builder
    @Schema(description = "타임슬롯 입력 DTO")
    public static class TimeSlotRequest {

        @Schema(description = "운영 요일 목록", example = "[\"MON\", \"TUE\"]")
        private List<DayOfWeek> days;

        @Schema(description = "시작 시간", example = "00:00")
        private LocalTime startTime;

        @Schema(description = "종료 시간", example = "07:00")
        private LocalTime endTime;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class TimeSlotResponse {
        @Schema(description = "요일", example = "Mon")
        private String dayOfWeek;

        @Schema(description = "시작 시간", example = "10:00")
        private String startTime;

        @Schema(description = "종료 시간", example = "17:00")
        private String endTime;

        public static TimeSlotResponse from(ResourceTimeSlots slot) {
            return TimeSlotResponse.builder()
                    .dayOfWeek(slot.getDayOfWeek().name())
                    .startTime(slot.getStartTime().toString())
                    .endTime(slot.getEndTime().toString())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TimeSlotExceptionResponse {
        @Schema(description = "예외 날짜", example = "2025-10-25")
        private String date;

        @Schema(description = "시작 시간", example = "10:00")
        private String startTime;

        @Schema(description = "종료 시간", example = "17:00")
        private String endTime;

        @Schema(description = "휴무 여부", example = "true")
        private boolean isClosed;

        @Schema(description = "비고", example = "공휴일")
        private String note;

        public static TimeSlotExceptionResponse from(ResourceTimeSlotExceptions ex) {
            return TimeSlotExceptionResponse.builder()
                    .date(ex.getDate().toString())
                    .startTime(ex.getStartTime().toString())
                    .endTime(ex.getEndTime().toString())
                    .isClosed(ex.getIsClosed())
                    .note(ex.getNote() == null ? "" : String.valueOf(ex.getNote()))
                    .build();
        }
    }
}
