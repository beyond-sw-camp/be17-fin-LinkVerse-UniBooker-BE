package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.user.model.entity.Users;

import java.util.List;

@Schema(description = "서비스 그룹 관련 DTO 클래스들")
public class ResourceGroupDto {

    @Getter
    @Builder
    @Schema(description = "서비스 그룹 생성 요청 DTO")
    public static class ResourceGroupRegisterReq {

        // TODO : 로그인 기능 개발되면 삭제
        private Long userId;

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private ServiceCategory category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        public ResourceGroups toEntity(Users authUser, Companies company) {
           return ResourceGroups.builder()
                    .name(name)
                    .description(description)
                    .thumbnail(thumbnail)
                    .category(category)
                    .isAlwaysAvailable(isAlwaysAvailable)
                    .isActive(true)
                    .company(company)
                    .createdBy(authUser)
                    .updatedBy(authUser)
                    .build();
        }
    }


    @Getter
    @Builder
    @Schema(description = "리소스 그룹 상세 조회 응답 DTO (리소스 그룹 수정용)")
    public static class ResourceGroupUpdateRes {

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private String category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;
    }


    @Getter
    @Builder
    @Schema(description = "리소스 그룹 상세 조회 응답 DTO")
    public static class ResourceGroupDetailRes {

        @Schema(description = "서비스 그룹 아이디", example = "1")
        private Long id;

        @Schema(description = "서비스 그룹 이름", example = "회의실")
        private String name;

        @Schema(description = "서비스 그룹 설명", example = "회의실 관련 예약/신청 서비스 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        public static ResourceGroupDetailRes fromEntity(ResourceGroups entity) {
            return ResourceGroupDetailRes.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .thumbnail(entity.getThumbnail())
                    .build();
        }
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

        @Schema(description = "리소스 그룹 설명", example = "동아리 관련 예약/신청 모음")
        private String description;

        @Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg")
        private String thumbnail;

        @Schema(description = "서비스 카테고리", example = "RESERVATION(예약형)/SEAT(좌석형)/EVENT(신청형)")
        private String category;

        @Schema(description = "상시 모집 여부", example = "true")
        private Boolean isAlwaysAvailable;
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
    }
}
