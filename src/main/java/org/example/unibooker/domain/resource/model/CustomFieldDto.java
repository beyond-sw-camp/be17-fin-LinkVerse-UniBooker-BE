package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "커스텀 필드 관련 DTO 클래스들")
public class CustomFieldDto {

    // ---------------- 필드 정의 --------------------

    @Getter
    @Builder
    @Schema(description = "커스텀 필드 생성 & 수정 요청 DTO")
    public static class CustomFieldReq {

        @Schema(description = "필드 이름", example = "회의실 이름")
        private String fieldName;

        @Schema(description = "설명", example = "예약하려는 회의실의 이름")
        private String description;

        @Schema(description = "데이터 타입", example = "STRING / NUMBER / DATE")
        private CustomDataType dataType;

        @Schema(description = "필드 유형", example = "SERVICE / USER")
        private CustomTargetType targetType;

        @Schema(description = "필수 여부", example = "true")
        private Boolean required;

        public CustomFieldDefinitions toEntity() {
            return CustomFieldDefinitions.builder()
                    .fieldName(fieldName)
                    .description(description)
                    .dataType(dataType)
                    .targetType(targetType)
                    .isRequired(required)
                    .build();
        }
    }


    @Getter
    @Builder
    @Schema(description = "커스텀 필드 조회 응답 DTO")
    public static class CustomFieldRes {

        @Schema(description = "필드 ID", example = "1")
        private Long id;

        @Schema(description = "필드 이름", example = "회의실 이름")
        private String fieldName;

        @Schema(description = "설명", example = "예약하려는 회의실의 이름")
        private String description;

        @Schema(description = "데이터 타입", example = "STRING / NUMBER / DATE")
        private String dataType;

        @Schema(description = "필드 유형", example = "SERVICE / USER")
        private String targetType;

        @Schema(description = "필수 여부", example = "true")
        private Boolean required;

        public static CustomFieldRes fromEntity(CustomFieldDefinitions entity) {
            return CustomFieldRes.builder()
                    .id(entity.getId())
                    .fieldName(entity.getFieldName())
                    .dataType(entity.getDataType().name()) // ENUM → 문자열
                    .targetType(entity.getTargetType().name()) // ENUM → 문자열
                    .required(entity.getIsRequired())
                    .description(entity.getDescription())
                    .build();
        }
    }



    // ---------------- 필드 값 --------------------

    @Getter
    @Builder
    @Schema(description = "커스텀 필드 값 생성 DTO")
    public static class CustomFieldValue {

        @Schema(description = "대상 리소스 ID 또는 사용자 ID", example = "101")
        private Long targetId;

        @Schema(description = "필드 ID", example = "1")
        private Long customFieldId;

        @Schema(description = "입력 값", example = "회의실 101")
        private String value;

        public UserCustomFieldValues toUserEntity(CustomFieldDefinitions field) {
            return UserCustomFieldValues.builder()
                    .reservationId(this.targetId)
                    .fieldValue(this.value)
                    .customFieldDefinition(field)
                    .build();
        }

        public ResourceCustomFieldValues toResourceEntity(CustomFieldDefinitions field) {
            return ResourceCustomFieldValues.builder()
                    .resourceId(this.targetId)
                    .fieldValue(this.value)
                    .customFieldDefinition(field)
                    .build();
        }
    }


    @Getter
    @Builder
    @Schema(description = "커스텀 필드 값 조회 응답 DTO")
    public static class CustomFieldValueListRes {

        @Schema(description = "필드 ID", example = "1")
        private Long customFieldId;

        @Schema(description = "필드 이름", example = "회의실 이름")
        private String fieldName;

        @Schema(description = "값", example = "101호 회의실")
        private String value;

        @Schema(description = "타겟 타입", example = "USER / RESOURCE")
        private CustomTargetType targetType;

        public static CustomFieldValueListRes fromUserEntity(UserCustomFieldValues entity) {
            return CustomFieldValueListRes.builder()
                    .customFieldId(entity.getCustomFieldDefinition().getId())
                    .fieldName(entity.getCustomFieldDefinition().getFieldName())
                    .value(entity.getFieldValue())
                    .targetType(CustomTargetType.USER)
                    .build();
        }

        public static CustomFieldValueListRes fromResourceEntity(ResourceCustomFieldValues entity) {
            return CustomFieldValueListRes.builder()
                    .customFieldId(entity.getCustomFieldDefinition().getId())
                    .fieldName(entity.getCustomFieldDefinition().getFieldName())
                    .value(entity.getFieldValue())
                    .targetType(CustomTargetType.RESOURCE)
                    .build();
        }
    }
}
