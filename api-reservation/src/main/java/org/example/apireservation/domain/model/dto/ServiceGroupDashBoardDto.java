package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.common.model.Gender;

/**
 * 서비스 그룹별 대시보드 DTO 모음
 * - 서비스별 성과, 이용자 통계, 시간대별 통계
 * - 변환 로직은 ReservationMapper에 있음
 */
public class ServiceGroupDashBoardDto {

    /**
     * 서비스별 성과 DTO
     */
    @Getter
    @Builder
    @Schema(description = "서비스별 성과")
    public static class ServicePerformanceCount {
        @Schema(description = "리소스 ID", example = "1")
        private Long resourceId;

        @Schema(description = "예약 수", example = "25")
        private Integer count;
    }

    /**
     * 이용자 수 DTO
     */
    @Getter
    @Builder
    @Schema(description = "이용자 수")
    public static class VisitorCount {
        @Schema(description = "총 예약 수", example = "100")
        private Integer total;

        @Schema(description = "중복 제거된 실 이용자 수", example = "75")
        private Integer count;
    }

    /**
     * 성별 예약 통계 DTO
     */
    @Getter
    @Builder
    @Schema(description = "성별 예약 통계")
    public static class GenderReservationCount {
        @Schema(description = "성별", example = "MALE")
        private Gender gender;

        @Schema(description = "예약 수", example = "45")
        private Integer count;
    }

    /**
     * 나이대별 예약 통계 DTO
     */
    @Getter
    @Builder
    @Schema(description = "나이대별 예약 통계")
    public static class AgeReservationCount {
        @Schema(description = "나이대", example = "20")
        private Integer age;

        @Schema(description = "예약 수", example = "30")
        private Integer count;
    }

    /**
     * 시간대별 예약 현황 DTO
     */
    @Getter
    @Builder
    @Schema(description = "시간대별 예약 현황")
    public static class TimeSlotReservationCount {
        @Schema(description = "시간 (0-23)", example = "14")
        private Integer hour;

        @Schema(description = "예약 수", example = "12")
        private Integer count;
    }

    /**
     * 리소스 그룹별 예약 수 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "리소스 그룹별 예약 수 응답")
    public static class GroupReservationCountResponse {
        @Schema(description = "리소스 그룹 ID", example = "1")
        private Long groupId;

        @Schema(description = "예약 수", example = "150")
        private int count;
    }
}