package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

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

//        @Schema(description = "서비스 이미지 URL 목록", example = "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
//        private List<ResourceImages> resourceImageUrls;

        @Schema(description = "시작 날짜", example = "2025.10.16", nullable = true)
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2025.10.18", nullable = true)
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

        public Resources toEntity(ResourceGroups group) {
            LocalDate today = LocalDate.now();

            // 날짜 유효성 체크
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
            }
            if (endDate != null && endDate.isBefore(today)) {
                throw new IllegalArgumentException("지난 예약 서비스는 생성할 수 없습니다.");
            }
            if (startDate != null && endDate != null && startDate.equals(endDate)) {
                if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
                    throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
                }
            }

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
                    .resourceGroup(group)
                    .startDate(startDate)
                    .endDate(endDate)
                    .startTime(startTime)
                    .endTime(endTime)
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

        @Schema(description = "서비스 이미지 URL 목록", example = "[\"https://example.com/img1.jpg\", \"https://example.com/img2.jpg\"]")
        private List<String> resourceImageUrls;

        @Schema(description = "시작 날짜", example = "2025.10.16", nullable = true)
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2025.10.18", nullable = true)
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
    }


    @Getter
    @Builder
    @Schema(description = "리소스 목록 조회 정보 DTO")
    public static class ResourceListInfo {

        @Schema(description = "서비스 이름", example = "회의실 101")
        private String name;

        @Schema(description = "서비스 설명", example = "회의실 101 예약용")
        private String description;

        @Schema(description = "서비스 이미지 URL", example = "https://example.com/img1.jpg")
        private String resourceImage;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 목록 조회 응답 DTO")
    public static class ResourceListRes {

        @Schema(description = "리소스 목록")
        private List<ResourceListInfo> resources;
    }


    @Getter
    @Builder
    @Schema(description = "서비스 삭제 요청 DTO")
    public static class ResourceGroupDeleteReq {

        @Schema(description = "삭제할 서비스 그룹 ID", example = "1")
        private Long id;
    }
}
