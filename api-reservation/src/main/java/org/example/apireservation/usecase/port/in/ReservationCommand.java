package org.example.apireservation.usecase.port.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.*;
import java.util.List;

/** Adapter.in 으로 들어오는 예약 요청 DTO */
@Getter
@Builder
@Schema(description = "예약시 필요 요청 정보")
public class ReservationCommand {

        @Schema(description = "예약할 날짜", example = "2025-10-16")
        private LocalDate date;

        @Schema(description = "예약할 시간", example = "10:00")
        private LocalTime time;

        @Schema(description = "인원수", example = "3")
        private Integer headCount;

        @Schema(description = "좌석 행", example = "1")
        private Integer row;

        @Schema(description = "좌석 열", example = "2")
        private Integer col;

        @Schema(description = "리소스에 등록되어 있는 사용자 입력 커스텀 필드 값")
        private List<CustomFieldValueCommand> customFieldValues;
}
