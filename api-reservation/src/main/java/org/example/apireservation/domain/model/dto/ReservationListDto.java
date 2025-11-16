package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.apireservation.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 목록
 * 응답할 데이터를 담은 DTO
 * 변환 로직은 Reservationapper에 있음 */
public class ReservationListDto {

    // ========================== 플랫폼 관리자 및 기업 관리자 용 ==========================
    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 응답 정보")
    public static class ResponseList {
        private List<Object> list;
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [예약형] 단일 응답 정보")
    public static class ReservationResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "예약한 리소스")
        private String resourceName;

        @Schema(description = "예약 상태", example = "CONFIRMED 및 CANCELED")
        private ReservationStatus status;

        @Schema(description = "예약 시작 일시")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시")
        private LocalDateTime endDate;
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [좌석형] 단일 응답 정보")
    public static class SeatResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "좌석 행")
        private Integer row;

        @Schema(description = "좌석 열")
        private Integer col;

        @Schema(description = "예약 상태", example = "CONFIRMED 및 CANCELED")
        private ReservationStatus status;

        @Schema(description = "예약 시작 일시")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시")
        private LocalDateTime endDate;
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [신청형] 단일 응답 정보")
    public static class EventResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "이메일")
        private String email;

        @Schema(description = "신청일")
        private LocalDateTime applicationDate;

        @Schema(description = "신청 상태")
        private ReservationStatus status;
    }


    // ========================== 일반 사용자용 ==========================
    @Getter
    @Builder
    @Schema(description = "일반 사용자 예약 목록 조회 응답 정보")
    public static class UserResponseList {
        List<UserResponse> reservations;
    }

    @Getter
    @SuperBuilder
    @Schema(description = "일반 사용자 예약 목록 조회 단일 응답 정보")
    public static class UserResponse extends ReservationDetailDto.Response {
        @Schema(description = "예약 시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;
    }
}
