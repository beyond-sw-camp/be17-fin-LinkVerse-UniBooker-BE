package org.example.apiresource.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.apiresource.domain.model.CustomDataType;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "커스텀 필드 관련 DTO 클래스들")
public class CustomFieldDto {

    // ---------------- 필드 정의 --------------------

    @Setter // 테스트용
    @NoArgsConstructor // ✅ 테스트용 기본 생성자 추가
    @AllArgsConstructor(access = AccessLevel.PUBLIC) // ✅ 명시적 public 생성자
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

        @Schema(description = "선택형 옵션 목록 (RADIO/CHECKBOX)", nullable = true)
        private List<String> options;
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

        @Setter
        @Schema(description = "선택형 옵션 목록 (RADIO/CHECKBOX)", nullable = true)
        private List<String> options;
    }



    // ---------------- 필드 값 --------------------

    @Getter
    @Builder
    @Schema(description = "커스텀 필드 값 생성 DTO")
    public static class CustomFieldValue {

        @Schema(description = "필드 ID", example = "1")
        private Long customFieldId;

        @Schema(description = "입력 값", example = "회의실 101")
        private List<String> values;
    }


    @Getter
    @Builder
    @Schema(description = "커스텀 필드 값 조회 응답 DTO")
    public static class CustomFieldValueListRes {

        @Schema(description = "필드 ID", example = "1")
        private Long customFieldId;

        @Schema(description = "필드 이름", example = "회의실 이름")
        private String fieldName;

        @Schema(description = "값 목록", example = "[\"101호 회의실\"]")
        private List<String> values;

        private static String convertBooleanValue(String value) {
            if ("true".equalsIgnoreCase(value)) return "예";
            if ("false".equalsIgnoreCase(value)) return "아니오";
            return value; // boolean이 아니면 원래 값 그대로
        }
    }



    @Getter
    @Builder
    @Schema(description = "RESOURCE 커스텀 필드 값 수정 요청 DTO")
    public static class CustomFieldValueUpdateReq {

        @Schema(description = "수정할 필드의 ID", example = "1")
        private Long customFieldId;

        @Schema(description = "수정할 필드 값의 ID", example = "1")
        private Long customFieldValueId;

        @Schema(description = "수정할 값", example = "회의실 2")
        private String value;
    }
}
