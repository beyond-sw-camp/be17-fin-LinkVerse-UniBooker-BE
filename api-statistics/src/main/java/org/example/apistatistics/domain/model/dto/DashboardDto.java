package org.example.apistatistics.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardDto {

    // 관리자 전체 대시보드에 필요한 리소스 그룹 정보
    @Getter
    @Builder
    @Schema(description = "관리자 대시보드 리소스 그룹 정보")
    public static class AdminDashboardGroupInfo {

        @Schema(description = "리소스 그룹 아이디")
        private Long id;

        @Schema(description = "리소스 그룹 이름")
        private String name;

        @Schema(description = "리소스 그룹 조회수")
        private int viewCount;

        @Schema(description = "리소스 그룹 활성화 여부")
        private Boolean isActive;

        @Schema(description = "리소스 그룹에 속한 리소스 수")
        private int serviceCount;
    }


    // 관리자 전체 대시보드에 필요한 리소스/리소스 그룹 데이터
    @Getter
    @Builder
    @Schema(description = "관리자 대시보드 리소스/리소스 그룹 정보")
    public static class AdminDashboardResourceGroup {

        @Schema(description = "리소스 그룹 정보")
        private List<AdminDashboardGroupInfo> groups;

        @Schema(description = "리소스 그룹 개수")
        private int groupCount;

        @Schema(description = "리소스 개수")
        private int resourceCount;
    }


    // 관리자 전체 대시보드에 필요한 리소스 그룹 별 예약 수 응답 DTO
    @Getter
    @Builder
    public static class GroupReservationCountResponse {
        private Long groupId;
        private int count;
    }


    @Getter
    @Builder
    public static class ReservationTrendRequest {
        private List<Long> groupIds;   // 리소스 그룹 ID 리스트
        private LocalDateTime from;    // 조회 시작일
        private LocalDateTime to;      // 조회 종료일
    }


    @Getter
    @Builder
    public static class DashboardReservationTrendResponse {

        private LocalDate date;     // 예약 날짜
        private Long groupId;   // 리소스 그룹명
        private int count;          // 해당 그룹의 예약 수
    }


    @Getter
    @Builder
    @Schema(description = "관리자 대시보드 요약 통계 정보")
    public static class AdminDashboardSummary {

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
    @Builder
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
    @Builder
    @Schema(description = "예약 트렌드 데이터 (날짜별 그룹별 예약 수 통계)")
    public static class ReservationTrend {

        @Schema(description = "통계 날짜 (yyyy-MM-dd)", example = "2025-10-15")
        private String date;

        @Schema(description = "그룹별 예약 수 (key: 그룹명, value: 예약 수)", example = "{\"회의실 예약\":50, \"스터디룸 예약\":30}")
        private Map<String, Integer> groups; // 그룹명 → 예약 수
    }


    @Getter
    @Builder
    @Schema(description = "관리자 대시보드 전체 응답 데이터")
    public static class AdminDashboardResponse {

        @Schema(description = "요약 통계 데이터 (총 예약 수, 활성 그룹 수 등)")
        private AdminDashboardSummary summary;

        @Schema(description = "서비스 그룹별 현황 데이터 목록")
        private List<ResourceGroupStats> serviceGroups;

        @Schema(description = "예약 트렌드 데이터 (날짜별 그룹별 예약 수)")
        private List<ReservationTrend> reservationTrends;
    }







    // 플랫폼 관리자 대시보드에 필요한 데이터


    @Getter
    @Builder
    @Schema(description = "플랫폼 관리자 대시보드 전체 응답 데이터")
    public static class SuperDashboardResponse {
        private CompanyStats companyStats;
        private CustomerStats customerStats;
        private ServiceStats serviceStats;
        private List<ErrorLogs> errorLogs;
    }

    @Getter
    @Builder
    public static class CompanyStats {
        private int currentCompanyCount; // 현재 가입 수
        private List<Integer> monthlyNewRegistrations; // 월별 신규 가입
    }

    @Getter
    @Builder
    public static class CustomerStats {
        private int currentCustomerCount; // 현재 가입 수
        private List<Integer> cumulativeRegistrations; // 누적 가입
    }

    @Getter
    @Builder
    public static class ServiceStats  {
        private int totalServiceCount; // 총 서비스 수
        private List<Integer> categoryCounts; // 예약, 좌석 예매, 이벤트 신청 순
        private List<String> categoryLabels; // ["예약", "좌석 예매", "이벤트 신청"]
    }

    @Getter
    @Builder
    public static class ErrorLogs {
        private String code;
        private String message;
        private String time; // 나중에 LocalDateTime 타입으로 변경 가능
    }








    @Getter
    @Builder
    public static class YearlyStatisticsResponse  {

        private List<Integer> monthlyNewCompanies;   // 1월~12월 기업 가입 수
        private List<Integer> monthlyNewCustomers;   // 1월~12월 고객 가입 수
        private int totalCompanies;                  // 전체 기업 누적 수
        private int totalCustomers;                  // 전체 고객 누적 수
    }


    @Getter
    @Builder
    public static class ServiceStatsResponse {
        private int totalServiceCount;
        private List<Integer> categoryCounts;
        private List<String> categoryLabels;
    }






    // 그룹별 대시보드
    @Getter
    @Builder
    public static class ResourceGroupDashboardResponse {
        private Long resourceGroupId;

        // 리소스 개수
        private int resourceCount;

        // 리소스별 예약 가능 시간 정보
        private List<ResourcePossibleTimeInfo> resources;

        // 조회수 통계
        private ViewStats viewStats;
    }


    @Getter
    @Builder
    public static class ResourcePossibleTimeInfo {
        private Long resourceId;
        private String resourceName;
        private int intervalMinutes;   // 리소스의 시간 간격
        private int possibleTimeCount; // 이번달 예약 가능한 시간 개수
    }


    @Getter
    @Builder
    public static class ViewStats {

        // 어제 ~ 현재 시각 기준 누적 조회수
        private int yesterdayAccumulatedViewCount;

        // 오늘 전체 누적 조회수
        private int todayTotalViewCount;

        // 시간대별 조회수 (예: { "00": 23, "01": 55, ... })
        private List<HourlyViewCount> hourlyViewCounts;
    }


    @Getter
    @Builder
    public static class HourlyViewCount {
        private int hour;     // 0~23
        private long viewCount;
    }


    // 응답
    @Getter
    @Builder
    public static class ResourceGroupDashboardData {
        private int resourceCount;
        private int cumReservationCount;
        private int cumCancleCount;
        private int totalCustomerCount;
        private int useCustomerCount;
        private List<PerformancePerResource> performanceByResources;
        private List<ReservationGenderInfo> reservationGenderInfos;
        private List<ReservationAgeInfo> reservationAgeInfos;
        private int yesterDayViewCount;
        private int todayViewCount;
        private List<HourlyViewCount> hourlyViewCounts;
        private List<TimeSlotReservationCount> houlryReservationCounts;
    }


    @Getter
    @Builder
    public static class PerformancePerResource {
        private String resourceName;
        private Integer count;
    }


    @Getter
    @Setter
    public class UserCountResponse {
        private int total;
        private int count;
    }


    @Getter
    @Builder
    public static class ReservationGenderInfo {
        private boolean isMale;
        private Integer count;
    }


    @Getter
    @Builder
    public static class ReservationAgeInfo {
        private Integer age;
        private Integer count;
    }



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

    // ========================== 시간대별 에약 현황 ==========================
    @Getter
    @Builder
    public static class TimeSlotReservationCount {
        private Integer hour;
        private Integer count;
    }
}