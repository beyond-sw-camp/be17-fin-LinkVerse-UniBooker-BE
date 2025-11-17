package org.example.apiresource.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.example.apiresource.domain.model.ServiceCategory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "서비스 그룹 관련 DTO 클래스들")
public class ResourceGroupDto {

    @Setter // 테스트용
    @NoArgsConstructor // ✅ 테스트용 기본 생성자 추가
    @AllArgsConstructor(access = AccessLevel.PUBLIC) // ✅ 명시적 public 생성자
    @Getter
    @Builder
    @Schema(description = "서비스 그룹 생성 요청 DTO")
    public static class ResourceGroupRegisterReq {

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 코드", example = "SRV001")
        private String groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private ServiceCategory category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "커스텀 필드 목록")
        private List<CustomFieldDto.CustomFieldReq> customFields;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 그룹 상세 조회 응답 DTO (리소스 그룹 수정용)")
    public static class ResourceGroupUpdateRes {

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 코드", example = "SRV001")
        private String groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private String category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "커스텀 필드 목록")
        private List<CustomFieldDto.CustomFieldRes> customFields;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 그룹 상세 조회 응답 DTO")
    public static class ResourceGroupDetailRes {

        @Schema(description = "서비스 그룹 아이디", example = "1")
        private Long id;

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 코드", example = "SRV001")
        private String groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "서비스 카테고리", example = "RESERVATION")
        private String category;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 그룹 생성일", example = "2025.10.13")
        private LocalDateTime createdAt;

        @Schema(description = "서비스 그룹 수정일", example = "2025.10.15")
        private LocalDateTime updatedAt;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 그룹 생성자", example = "유현경")
        private String administrator;

        @Schema(description = "서비스 그룹 수정자", example = "김철수")
        private String updatedByName;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "서비스 개수", example = "5")
        private int serviceCount;

        @Schema(description = "진행중인 서비스 개수", example = "3")
        private int activeServiceCount;

        @JsonProperty("isActive")
        @Schema(description = "서비스 그룹의 상태", example = "true")
        private Boolean isActive;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 그룹의 카테고리", example = "RESERVATION/SEAT/EVENT")
        private String serviceCategory;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;
    }


    @Getter
    @Builder
    @Schema(description = "특정 기업의 리소스 그룹 목록 조회 응답 DTO")
    public static class ResourceGroupListRes {

        @Schema(description = "리소스 그룹 목록")
        private List<ResourceGroupDetailRes> resourceGroups;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 그룹 수정 요청 DTO")
    public static class ResourceGroupUpdateReq {

        @Schema(description = "리소스 그룹 이름", example = "동아리")
        private String name;

        @Schema(description = "리소스 그룹 코드", example = "SRV001")
        private String groupCode;  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항

        @Schema(description = "리소스 그룹 설명", example = "동아리 관련 예약/신청 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private String category;

        @Schema(description = "서비스 그룹의 상태", example = "true")
        private Boolean isActive;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "커스텀 필드 목록")
        private List<CustomFieldDto.CustomFieldReq> customFields;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 생성을 위한 필수입력 필드 응답 DTO")
    public static class ServiceRegisterFieldRes {

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "카테고리", example = "RESERVATION")
        private ServiceCategory category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "커스텀 필드 목록")
        private List<CustomFieldDto.CustomFieldRes> customFields;
    }


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


    @Getter
    @Builder
    public static class ServiceStatsResponse {
        private int totalServiceCount;
        private List<Integer> categoryCounts;
        private List<String> categoryLabels;
    }


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
        private long yesterdayAccumulatedViewCount;

        // 오늘 전체 누적 조회수
        private long todayTotalViewCount;

        // 시간대별 조회수 (예: { "00": 23, "01": 55, ... })
        private List<HourlyViewCount> hourlyViewCounts;
    }


    @Getter
    @Builder
    public static class HourlyViewCount {
        private int hour;     // 0~23
        private long viewCount;
    }
}
