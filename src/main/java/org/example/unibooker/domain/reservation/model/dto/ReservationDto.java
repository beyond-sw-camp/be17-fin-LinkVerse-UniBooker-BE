package org.example.unibooker.domain.reservation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;

import java.time.*;
import java.util.*;

/**
 * Request - 예약 요청
 * Response - 예약 조회 응답
 **/
public class ReservationDto {

    // ===================
    // 예약 요청 DTO
    // ===================
    @Getter
    @Schema(description = "예약시 필요 요청 정보")
    public class Request {
        @Schema(description = "예약할 날짜", example = "2025-10-16")
        private LocalDate date;

        @Schema(description = "예약할 시간", example = "10:00")
        private LocalTime time;

        @Schema(description = "인원수", example = "3")
        private Integer headCount;

        @Schema(description = "관리자가 커스텀으로 정의한 필드",
                example = "{\"department_name\": \"인사부\", \"request_note\": \"회의실에 빔프로젝터 필요\"}")
        private Map<String, Object> customFields;
    }


    // ===================
    // 예약 응답 DTO
    // ===================
    @Schema(description = "예약 조회 응답 정보")
    public class Response {
        @Schema(description = "예약 번호", example = "1")
        private Integer id;

        @Schema(description = "예약자", example = "유현경")
        private String username;

        @Schema(description = "예약 상태", example = "CONFIRMED")
        private ReservationStatus status;

        @Schema(description = "시작 일시", example = "2025-10-16 10:00:00")
        private LocalDateTime startTime;

        @Schema(description = "종료 일시", example = "2025-10-16 11:00:00")
        private LocalDateTime endTime;

        @Schema(description = "예약한 서비스 항목의 서비스", example = "회의실")
        private String resourceGroup;

        @Schema(description = "예약한 서비스 항목", example = "회의실A")
        private String resource;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;
    }
}
