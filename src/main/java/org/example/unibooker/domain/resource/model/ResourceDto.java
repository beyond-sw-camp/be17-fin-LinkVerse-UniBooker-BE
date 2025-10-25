package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        // 입력값 검증 함수
        public void validate() {
            // 종료일 체크
            if (endDate != null && endDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("종료일이 오늘 이전인 리소스는 생성할 수 없습니다.");
            }
            // 시작일/종료일 체크
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
            }
            // 시간 간격 검증
            if (timeInterval != 30 && timeInterval != 60) {
                throw new IllegalArgumentException("timeInterval은 30 또는 60만 가능합니다.");
            }
        }

        public Resources toEntity(ResourceGroups group) {
            LocalDate today = LocalDate.now();

            // status 결정
            ResourceStatus status;
            boolean alwaysAvailable = Boolean.TRUE.equals(group.getIsAlwaysAvailable());
            boolean startDatePassed = startDate != null && !startDate.isAfter(today);

            if (alwaysAvailable || startDatePassed) {
                status = ResourceStatus.IN_PROGRESS; // 진행 중
            } else {
                status = ResourceStatus.PROGRESS_BEFORE; // 진행 전
            }

            // Entity 생성
            return Resources.builder()
                    .name(name)
                    .description(description)
                    .resourceImage(resourceImage)
                    .resourceGroup(group)
                    .startDate(startDate)
                    .endDate(endDate)
                    .timeInterval(timeInterval)
                    .capacity(capacity)
                    .row(row)
                    .col(col)
                    .status(status)
                    .build();
        }
    }


    @Getter
    @Builder
    @Schema(description = "서비스 상세 조회 응답 DTO (리소스 수정용 & 상세 조회용)")
    public static class ResourceUpdateRes {

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

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

        @Schema(description = "운영 시간 목록")
        private List<TimeSlotDto.TimeSlotResponse> timeSlots;

        @Schema(description = "예외 시간 목록 (휴무일 등)")
        private List<TimeSlotDto.TimeSlotExceptionResponse> exceptionSlots;


        public static ResourceUpdateRes fromEntity(Resources resource) {
            return new ResourceUpdateRes(
                    resource.getName(),
                    resource.getDescription(),
                    resource.getResourceImage(),
                    resource.getStartDate(),
                    resource.getEndDate(),
                    resource.getTimeInterval(),
                    resource.getCapacity(),
                    resource.getRow(),
                    resource.getCol(),
                    Optional.ofNullable(resource.getTimeSlots())
                    .orElse(List.of())
                    .stream()
                    .map(TimeSlotDto.TimeSlotResponse::from)
                    .collect(Collectors.toList()),
                    Optional.ofNullable(resource.getTimeSlotExceptions())
                            .orElse(List.of())
                            .stream()
                            .map(TimeSlotDto.TimeSlotExceptionResponse::from)
                            .collect(Collectors.toList())
            );
        }
    }


    @Getter
    @Builder
    @Schema(description = "리소스 목록 조회 정보 DTO")
    public static class ResourceListInfo {

        @Schema(description = "서비스 아이디", example = "회의실 101")
        private Long id;

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

        @Schema(description = "서비스 이미지 URL", example = "https://example.com/img1.jpg")
        private String resourceImage;

        @Schema(description = "서비스 상태", example = "PROGRESS_BEFORE/PROGRESS_BEFORE/CLOSE")
        private ResourceStatus status;

        @Schema(description = "생성자 이름", example = "김한화")
        private String createdByName;

        @Schema(description = "생성일자", example = "2025.10.20")
        private String updatedAt;

        @Schema(description = "인원수", example = "7")
        private String capacity;

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        public static ResourceListInfo fromEntity(Resources resource) {
            return new ResourceListInfo(
                    resource.getId(),
                    resource.getName(),
                    resource.getDescription(),
                    resource.getResourceImage(),
                    resource.getStatus(),
                    resource.getCreatedBy() != null ? resource.getCreatedBy().getName() : null,
                    resource.getUpdatedAt() != null ? resource.getUpdatedAt().format(DATE_FORMATTER) : null,
                    resource.getCapacity() != null ? resource.getCapacity().toString() : null
            );
        }
    }


    @Getter
    @Builder
    @Schema(description = "리소스 목록 조회 응답 DTO")
    public static class ResourceListRes {

        @Schema(description = "리소스 목록")
        private List<ResourceListInfo> resources;

        public static ResourceListRes fromEntity(List<ResourceListInfo> resourceList) {
            return ResourceListRes.builder()
                    .resources(resourceList)
                    .build();
        }
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
        private List<TimeSlotDto.TimeSlotResponse> timeSlots;

        @Schema(description = "예외 타임슬롯 목록", nullable = true)
        private List<TimeSlotDto.TimeSlotExceptionResponse> exceptionSlots;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 삭제 요청 DTO")
    public static class ResourceGroupDeleteReq {

        @Schema(description = "삭제할 서비스 그룹 ID", example = "1")
        private Long id;
    }
}