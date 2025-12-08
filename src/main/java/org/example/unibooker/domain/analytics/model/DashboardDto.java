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
    @Schema(description = "관리자 대시보드 전체 응답 데이터")
    public static class AdminDashboardResponse {

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
    @Schema(description = "관리자 대시보드 요약 통계 정보")
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

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "플랫폼 관리자 대시보드 전체 응답 데이터")
    public static class SuperDashboardResponse {
        private CompanyStats companyStats;
        private CustomerStats customerStats;
        private ServiceStats serviceStats;
        private List<ErrorLogs> errorLogs;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "기업 통계 정보")
    public static class CompanyStats {

        @Schema(description = "현재 기업 수", example = "50")
        private int currentCompanyCount;

        @Schema(description = "월별 신규 가입 수")
        private List<Integer> monthlyNewRegistrations;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "고객 통계 정보")
    public static class CustomerStats {

        @Schema(description = "현재 고객 수", example = "1500")
        private int currentCustomerCount;

        @Schema(description = "누적 가입 수")
        private List<Integer> cumulativeRegistrations;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "서비스 통계 정보")
    public static class ServiceStats {

        @Schema(description = "총 서비스 수", example = "120")
        private int totalServiceCount;

        @Schema(description = "카테고리별 개수 (예약, 좌석, 이벤트 순)")
        private List<Integer> categoryCounts;

        @Schema(description = "카테고리 라벨", example = "[\"예약\", \"좌석 예매\", \"이벤트 신청\"]")
        private List<String> categoryLabels;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "에러 로그 요약 정보")
    public static class ErrorLogs {

        @Schema(description = "에러 코드", example = "500")
        private String code;

        @Schema(description = "에러 메시지", example = "Internal Server Error")
        private String message;

        @Schema(description = "발생 시간", example = "2025-10-20T15:30:00")
        private String time;
    }

    // ==================== 리소스 그룹별 대시보드 ====================

    /**
     * 리소스 그룹별 대시보드 응답
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 그룹별 대시보드 응답")
    public static class ResourceGroupDashboardResponse {

        @Schema(description = "총 서비스 수", example = "5")
        private int resourceCount;

        @Schema(description = "누적 예약 수", example = "120")
        private int cumReservationCount;

        @Schema(description = "누적 취소 수", example = "15")
        private int cumCancleCount;

        @Schema(description = "이용자 수 (예약한 고유 사용자)", example = "45")
        private int useCustomerCount;

        @Schema(description = "전체 고객 수", example = "200")
        private int totalCustomerCount;

        @Schema(description = "서비스별 성과 리스트")
        private List<ResourcePerformance> performanceByResources;

        @Schema(description = "오늘 조회 수", example = "85")
        private int todayViewCount;

        @Schema(description = "어제 조회 수", example = "72")
        private int yesterDayViewCount;

        @Schema(description = "시간별 예약 수")
        private List<HourlyCount> houlryReservationCounts;

        @Schema(description = "시간별 조회 수")
        private List<HourlyViewCount> hourlyViewCounts;

        @Schema(description = "성별 통계")
        private GenderStats genderStats;

        @Schema(description = "연령대별 통계")
        private List<AgeGroupStats> ageGroupStats;
    }

    /**
     * 서비스별 성과
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "서비스별 성과")
    public static class ResourcePerformance {

        @Schema(description = "서비스명", example = "대회의실 A")
        private String resourceName;

        @Schema(description = "예약 비율 (%)", example = "25.5")
        private double count;
    }

    /**
     * 시간별 예약 수
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "시간별 예약 수")
    public static class HourlyCount {

        @Schema(description = "시간 (0-23)", example = "14")
        private int hour;

        @Schema(description = "예약 수", example = "12")
        private int count;
    }

    /**
     * 시간별 조회 수
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "시간별 조회 수")
    public static class HourlyViewCount {

        @Schema(description = "시간 (0-23)", example = "14")
        private int hour;

        @Schema(description = "조회 수", example = "45")
        private int viewCount;
    }

    /**
     * 성별 통계
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "성별 통계")
    public static class GenderStats {

        @Schema(description = "남성 수", example = "120")
        private int maleCount;

        @Schema(description = "여성 수", example = "80")
        private int femaleCount;

        @Schema(description = "미정의 수", example = "10")
        private int undefinedCount;

        @Schema(description = "남성 비율 (%)", example = "57.1")
        private double malePercent;

        @Schema(description = "여성 비율 (%)", example = "38.1")
        private double femalePercent;

        @Schema(description = "미정의 비율 (%)", example = "4.8")
        private double undefinedPercent;
    }

    /**
     * 연령대별 통계
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "연령대별 통계")
    public static class AgeGroupStats {

        @Schema(description = "연령대", example = "20대")
        private String ageGroup;

        @Schema(description = "인원 수", example = "45")
        private int count;

        @Schema(description = "비율 (%)", example = "35.5")
        private double percent;
    }
}