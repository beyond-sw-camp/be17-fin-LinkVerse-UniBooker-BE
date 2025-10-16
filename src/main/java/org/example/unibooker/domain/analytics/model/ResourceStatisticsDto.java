package org.example.unibooker.domain.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Schema(description = "리소스 통계 DTO")
public class ResourceStatisticsDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 통계 생성/업데이트 요청 정보")
    public static class Request {
        @Schema(description = "리소스 ID", example = "10")
        private Long resourceId;

        @Schema(description = "총 예약 건수", example = "100")
        private Integer totalReservations;

        @Schema(description = "성공 예약 건수", example = "80")
        private Integer successfulReservations;

        @Schema(description = "취소 예약 건수", example = "20")
        private Integer cancelledReservations;

        @Schema(description = "중복 예약 건수", example = "5")
        private Integer duplicateReservations;

        @Schema(description = "이용률", example = "0.8")
        private Float utilizationRate;

        @Schema(description = "조회 수", example = "500")
        private Integer totalViews;

        @Schema(description = "피크 시간대", example = "14")
        private Integer peakHour;

        @Schema(description = "피크 예약 수", example = "10")
        private Integer peakReservations;

        @Schema(description = "통계 기준 날짜")
        private LocalDate statStartDate;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 통계 응답 정보")
    public static class Response {
        @Schema(description = "통계 ID", example = "1")
        private Long id;

        @Schema(description = "리소스 ID", example = "10")
        private Long resourceId;

        @Schema(description = "총 예약 건수", example = "100")
        private Integer totalReservations;

        @Schema(description = "성공 예약 건수", example = "80")
        private Integer successfulReservations;

        @Schema(description = "취소 예약 건수", example = "20")
        private Integer cancelledReservations;

        @Schema(description = "중복 예약 건수", example = "5")
        private Integer duplicateReservations;

        @Schema(description = "이용률", example = "0.8")
        private Float utilizationRate;

        @Schema(description = "조회 수", example = "500")
        private Integer totalViews;

        @Schema(description = "피크 시간대", example = "14")
        private Integer peakHour;

        @Schema(description = "피크 예약 수", example = "10")
        private Integer peakReservations;

        @Schema(description = "통계 기준 날짜")
        private LocalDate statStartDate;
    }
}
