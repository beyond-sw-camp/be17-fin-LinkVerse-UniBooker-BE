package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @Schema(description = "예외 타임슬롯 입력 DTO")
    public static class ExceptionSlotRequest {

        @Schema(description = "예외 날짜", example = "2025.10.25")
        private LocalDate date;

        @Schema(description = "시작 시간", example = "00:00")
        private LocalTime startTime; // 휴무이면 null 가능

        @Schema(description = "종료 시간", example = "12:00")
        private LocalTime endTime;   // 휴무이면 null 가능

        @Schema(description = "휴무 여부", example = "false")
        private Boolean isClosed;

        @Schema(description = "비고", example = "개인 사정으로 오전에만 운영합니다.")
        private String note;

        public ResourceTimeSlotExceptions toEntity(Resources resource) {
            return ResourceTimeSlotExceptions.builder()
                    .resources(resource)
                    .date(this.date)
                    .startTime(this.isClosed ? null : this.startTime)
                    .endTime(this.isClosed ? null : this.endTime)
                    .isClosed(this.isClosed)
                    .note(this.note)
                    .build();
        }
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class TimeSlotResponse {
        @Schema(description = "요일", example = "Mon")
        private String dayOfWeek;

        @Schema(description = "시작 시간", example = "10:00")
        private LocalTime startTime;

        @Schema(description = "종료 시간", example = "17:00")
        private LocalTime endTime;

        public static TimeSlotResponse fromEntity(ResourceTimeSlots slot) {
            return TimeSlotResponse.builder()
                    .dayOfWeek(slot.getDayOfWeek().name())
                    .startTime(slot.getStartTime())
                    .endTime(slot.getEndTime())
                    .build();
        }

        public static TimeSlotResponse from(LocalTime startTime, LocalTime endTime, DayOfWeek dayOfWeek) {
            return TimeSlotResponse.builder()
                    .dayOfWeek(dayOfWeek.name())
                    .startTime(startTime)
                    .endTime(endTime)
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
        private LocalTime startTime;

        @Schema(description = "종료 시간", example = "17:00")
        private LocalTime endTime;

        @Schema(description = "휴무 여부", example = "true")
        private boolean isClosed;

        @Schema(description = "비고", example = "공휴일")
        private String note;

        public static TimeSlotExceptionResponse fromEntity(ResourceTimeSlotExceptions ex) {
            return TimeSlotExceptionResponse.builder()
                    .date(ex.getDate().toString())
                    .startTime(ex.getStartTime())
                    .endTime(ex.getEndTime())
                    .isClosed(ex.getIsClosed())
                    .note(ex.getNote() == null ? "" : String.valueOf(ex.getNote()))
                    .build();
        }
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyTimeSlotResponse {
        private String date; // yyyy-MM-dd
        private boolean isClosed; // 휴무 여부
        private List<TimeSlotResponse> slots; // 정규 시간 슬롯
        private String note;

        public static DailyTimeSlotResponse fromEntity(LocalDate date, boolean isClosed, String note, List<TimeSlotDto.TimeSlotResponse> slotResponses) {
            return DailyTimeSlotResponse.builder()
                    .date(date.toString())
                    .isClosed(isClosed)
                    .note(note)
                    .slots(slotResponses)
                    .build();
        }
    }
}
