package org.example.unibooker.domain.resource.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "서비스 그룹 관련 DTO 클래스들")
public class ResourceGroupDto {

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

        public ResourceGroups toEntity(Users authUser, Companies company) {
           return ResourceGroups.builder()
                    .name(name)
                    .groupCode(groupCode)  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항
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

        public static ResourceGroupUpdateRes fromEntity(ResourceGroups group) {
            List<CustomFieldDto.CustomFieldRes> customFields = group.getCustomFieldDefinitions()
                    .stream()
                    .map(CustomFieldDto.CustomFieldRes::fromEntity) // 엔티티 → DTO 변환
                    .collect(Collectors.toList());

            return ResourceGroupUpdateRes.builder()
                    .name(group.getName())
                    .groupCode(group.getGroupCode())  // 서비스 그룹 목록 프론트 구조에 맞춘 추가사항
                    .description(group.getDescription())
                    .thumbnail(group.getThumbnail())
                    .category(group.getCategory().name())
                    .isAlwaysAvailable(group.getIsAlwaysAvailable())
                    .customFields(customFields)
                    .build();
        }
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

        public static ResourceGroupDetailRes fromEntity(ResourceGroups entity) {
            int activeServiceCount = (int) entity.getResources().stream()
                    .filter(Resources::getIsActive)
                    .count();

            return ResourceGroupDetailRes.builder()
                    .id(entity.getId())
                    .name(entity.getName())
                    .groupCode(entity.getGroupCode())
                    .description(entity.getDescription())
                    .category(entity.getCategory() != null ? entity.getCategory().name() : null)
                    .thumbnail(entity.getThumbnail())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .administrator(entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null)
                    .updatedByName(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getName() : null)
                    .serviceCount(entity.getResources().size())
                    .activeServiceCount(activeServiceCount)
                    .serviceCategory(entity.getCategory().name())
                    .isActive(entity.getIsActive() != null ? entity.getIsActive() : false)
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

        public List<CustomFieldDefinitions> toCustomFieldEntities() {
            if (customFields == null) return new ArrayList<>();
            return customFields.stream()
                    .map(CustomFieldDto.CustomFieldReq::toEntity)
                    .collect(Collectors.toList());
        }

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

        public static ServiceRegisterFieldRes fromEntity(ResourceGroups entity) {
            return ServiceRegisterFieldRes.builder()
                    .name(entity.getName())
                    .category(entity.getCategory())
                    .isAlwaysAvailable(entity.getIsAlwaysAvailable())
                    .customFields(entity.getCustomFieldDefinitions()
                            .stream().map(CustomFieldDto.CustomFieldRes::fromEntity).toList())
                    .build();
        }
    }
}
