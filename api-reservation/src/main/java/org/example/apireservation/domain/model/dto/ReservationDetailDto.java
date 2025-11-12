package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.apireservation.domain.model.ReservationStatus;
import org.example.apireservation.domain.model.ServiceCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 상세
 * 응답할 데이터를 담은 DTO */
public class ReservationDetailDto {

    // ========================== 예약 상세 조회 [공통] 응답 정보 ==========================
    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [공통] 응답 정보")
    public abstract static class Response {

        @Schema(description = "예약 번호", example = "1")
        private Long id;

        @Schema(description = "예약자", example = "유현경")
        private String userName;

        @Schema(description = "예약 상태", example = "CONFIRMED 및 CANCELED")
        private ReservationStatus status;

        @Schema(description = "예약한 리소스 그룹의 이미지")
        private String thumbnail;

        @Schema(description = "예약한 리소스의 리소스 그룹명", example = "회의실")
        private String resourceGroupName;

        @Schema(description = "예약한 리소스명", example = "회의실A")
        private String resourceName;

        @Schema(description = "예약한 리소스의 카테고리", example = "RESERVATION/SEAT/EVENT")
        private ServiceCategory serviceCategory;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        @Schema(description = "삭제일시")
        private LocalDateTime deletedAt;
    }


    // ========================== 예약 상세 조회 [예약형] 응답 정보 ==========================
    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [예약형] 응답 정보")
    public static class ReservationResponse extends Response {

        @Schema(description = "예약 시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        @Schema(description = "인원수")
        private Integer headCount;

        @Schema(description = "예약할 때 작성한 사용자 입력 커스텀 필드 값")
        private List<CustomFieldValueDto> customFieldValues;
    }


    // ========================== 예약 상세 조회 [좌석형] 응답 정보 ==========================
    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [좌석형] 응답 정보")
    public static class SeatResponse extends Response {

        @Schema(description = "예약 시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        @Schema(description = "인원수")
        private Integer headCount;

        @Schema(description = "좌석 행")
        private Integer row;

        @Schema(description = "좌석 열")
        private Integer col;

        @Schema(description = "예약할 때 작성한 사용자 입력 커스텀 필드 값")
        private List<CustomFieldValueDto> customFieldValues;
    }


    // ========================== 예약 상세 조회 [신청형] 응답 정보 ==========================
    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [신청형] 응답 정보")
    public static class EventResponse extends Response {

        @Schema(description = "예약할 때 작성한 사용자 입력 커스텀 필드 값")
        private List<CustomFieldValueDto> customFieldValues;
    }
}