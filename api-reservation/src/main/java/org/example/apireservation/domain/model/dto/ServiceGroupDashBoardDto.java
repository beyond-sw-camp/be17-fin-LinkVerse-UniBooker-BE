package org.example.apireservation.domain.model.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.apireservation.domain.model.Gender;

/**
 * 서비스 그룹별 대시보드에 필요한 DTO
 * 변환 로직은 ReservationMapper에 있음 */
public class ServiceGroupDashBoardDto {

    // ========================== 서비스별 성과 ==========================
    @Getter
    @Builder
    public static class ServicePerformanceCount {
        private Long resourceId;
        private Integer count;
    }

    // ========================== 이용자 수 ==========================
    @Getter
    @Builder
    public static class VisitorCount {
        private Integer total;
        private Integer count;
    }

    // ========================== 성별 ==========================
    @Getter
    @Builder
    public static class GenderReservationCount {
        private Gender gender;
        private Integer count;
    }

    // ========================== 나이대 ==========================
    @Getter
    @Builder
    public static class AgeReservationCount {
        private Integer age;
        private Integer count;
    }

    // ========================== 시간대별 에약 현황 ==========================
    @Getter
    @Builder
    public static class TimeSlotReservationCount {
        private Integer hour;
        private Integer count;
    }


    @Getter
    @Builder
    public static class GroupReservationCountResponse {
        private Long groupId;
        private int count;
    }
}