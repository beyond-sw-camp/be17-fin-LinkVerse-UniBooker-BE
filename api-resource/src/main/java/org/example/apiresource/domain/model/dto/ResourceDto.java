package org.example.apiresource.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.apiresource.domain.model.ResourceStatus;
import org.example.apiresource.domain.model.ServiceCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "서비스 관련 DTO 클래스들")
public class ResourceDto {

    @Getter
    @Builder
    @Schema(description = "서비스 생성 요청 DTO")
    public static class ResourceRegisterReq {

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

        @Schema(description = "서비스가 속한 그룹 ID", example = "1")
        private Long resourceGroupId;

        @Schema(description = "서비스 이미지 URL", example = "https://example.com/img1.jpg")
        private String resourceImage;

        @Schema(description = "시작 날짜", example = "2025.10.16", nullable = true)
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2025.10.18", nullable = true)
        private LocalDate endDate;

        @Schema(description = "시간 간격", example = "30 또는 60", nullable = true)
        private int timeInterval;

        @Schema(description = "인원수", example = "4", nullable = true)
        private Integer capacity;

        @Schema(description = "행", example = "4", nullable = true)
        private Integer row;

        @Schema(description = "열", example = "4", nullable = true)
        private Integer col;

        @Schema(description = "커스텀 필드 값 목록 (RESOURCE 타입)", nullable = true)
        private List<CustomFieldDto.CustomFieldValue> customFieldValues;

        @Schema(description = "타임슬롯 목록", nullable = true)
        private List<TimeSlotDto.TimeSlotRequest> timeSlots;

        @Schema(description = "예외 타임슬롯 목록", nullable = true)
        private List<TimeSlotDto.ExceptionSlotRequest> exceptionSlots;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 상세 조회 응답 DTO (리소스 수정용 & 상세 조회용)")
    public static class ResourceDetailInfo {

        @Schema(description = "서비스 그룹 아이디", example = "1")
        private Long resourceGroupId;

        @Schema(description = "서비스 그룹명", example = "회의실 예약")
        private String resourceGroupName;

        @Schema(description = "서비스 아이디", example = "1")
        private Long id;

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

        @Schema(description = "서비스 이미지 URL", example = "https://example.com/img1.jpg")
        private String resourceImage;

        @Schema(description = "서비스 상태", example = "PROGRESS_BEFORE/PROGRESS_BEFORE/CLOSE")
        private ResourceStatus status;

//        @Schema(description = "생성자 이름", example = "김한화")
//        private String createdByName;

        @Schema(description = "업데이트 날짜", example = "2025.10.20")
        private String updatedAt;

        @Schema(description = "시작 날짜", example = "2025.10.16", nullable = true)
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2025.10.18", nullable = true)
        private LocalDate endDate;

        @Schema(description = "시간 간격", example = "30 또는 60", nullable = true)
        private int timeInterval;

        @Schema(description = "인원수", example = "4", nullable = true)
        private Integer capacity;

        @Schema(description = "행", example = "4", nullable = true)
        private Integer row;

        @Schema(description = "열", example = "4", nullable = true)
        private Integer col;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private ServiceCategory category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "수정 버전", example = "5")
        private Long version;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 목록 조회 응답 DTO")
    public static class ResourceListRes {

        @Schema(description = "리소스 목록")
        private List<ResourceDetailInfo> resources;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 수정 요청 DTO")
    public static class ResourceUpdateReq {

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

        @Schema(description = "서비스 이미지 URL", example = "https://example.com/img1.jpg")
        private String resourceImage;

        @Schema(description = "시작 날짜", example = "2025-10-16", nullable = true)
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2025-10-18", nullable = true)
        private LocalDate endDate;

        @Schema(description = "시작 시간", example = "12:00", nullable = true)
        private LocalTime startTime;

        @Schema(description = "종료 시간", example = "19:00", nullable = true)
        private LocalTime endTime;

        @Schema(description = "시간 간격", example = "30 또는 60", nullable = true)
        private int timeInterval;

        @Schema(description = "인원수", example = "4", nullable = true)
        private Integer capacity;

        @Schema(description = "행", example = "4", nullable = true)
        private Integer row;

        @Schema(description = "열", example = "4", nullable = true)
        private Integer col;

        @Schema(description = "타임슬롯 목록", nullable = true)
        private List<TimeSlotDto.TimeSlotRequest> timeSlots;

        @Schema(description = "예외 타임슬롯 목록", nullable = true)
        private List<TimeSlotDto.ExceptionSlotRequest> exceptionSlots;
    }

    @Getter
    @Setter
    @Schema(description = "서비스 상태 변경 요청 DTO")
    public static class ResourceStatusChangReq{
        private Long resourceId;
        private Long version;
        private String targetStatus;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 삭제 요청 DTO")
    public static class ResourceGroupDeleteReq {

        @Schema(description = "삭제할 서비스 그룹 ID", example = "1")
        private Long id;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 존재 여부 확인 응답 DTO")
    public static class ResourceExistenceDto {
        private Long id;
        private ServiceCategory serviceCategory;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer timeInterval;
        private Integer capacity;
        private boolean isActive;
    }
}