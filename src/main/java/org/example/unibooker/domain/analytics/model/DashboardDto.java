package org.example.unibooker.domain.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@Schema(description = "대시보드 데이터 DTO")
public class DashboardDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "대시보드 전체 응답 데이터")
    public static class DashboardResponse {

        @Schema(description = "요약 통계 데이터 (총 예약 수, 활성 그룹 수 등)")
        private Summary summary;

        @Schema(description = "서비스 그룹별 현황 데이터 목록")
        private List<ResourceGroupStats> serviceGroups;

        @Schema(description = "예약 트렌드 데이터 (날짜별 그룹별 예약 수)")
        private List<ReservationTrend> reservationTrends;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "대시보드 요약 통계 정보")
    public static class Summary {

        @Schema(description = "총 예약 수", example = "1284")
        private long totalReservations;

        @Schema(description = "운영 중인 서비스 그룹 수", example = "6")
        private long activeServiceGroups;

        @Schema(description = "운영 중인 개별 서비스 수 (리소스 수)", example = "18")
        private long activeServices;

        @Schema(description = "전체 사용자 수", example = "342")
        private long userCount;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "서비스 그룹별 현황 데이터")
    public static class ResourceGroupStats {

        @Schema(description = "서비스 그룹명", example = "회의실 예약")
        private String name;

        @Schema(description = "해당 그룹에 속한 서비스(리소스) 수", example = "5")
        private int serviceCount;

        @Schema(description = "해당 그룹의 총 예약 수", example = "320")
        private long reservationCount;

        @Schema(description = "서비스 그룹 상태 (예: 운영 중, 비활성화)", example = "운영 중")
        private String status;

        @Schema(description = "그룹 페이지 조회수", example = "1540")
        private long viewCount;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "예약 트렌드 데이터 (날짜별 그룹별 예약 수 통계)")
    public static class ReservationTrend {

        @Schema(description = "통계 날짜 (yyyy-MM-dd)", example = "2025-10-15")
        private String date;

        @Schema(description = "그룹별 예약 수 (key: 그룹명, value: 예약 수)", example = "{\"회의실 예약\":50, \"스터디룸 예약\":30}")
        private Map<String, Integer> groups; // 그룹명 → 예약 수
    }
}