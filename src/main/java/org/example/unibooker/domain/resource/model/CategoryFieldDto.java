package org.example.unibooker.domain.resource.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "카테고리 별 필수 필드 관련 DTO 클래스들")
public class CategoryFieldDto {

    @Getter
    @Builder
    @Schema(description = "카테고리 필수 입력 필드 생성 & 수정 요청 DTO")
    public static class CategoryFieldReq {

        @Schema(description = "필드 이름", example = "인원수")
        private String fieldName;

        @Schema(description = "필드 설명", example = "수용 가능한 인원 수를 입력해주세요.")
        private String description;

        @Schema(description = "데이터 타입", example = "TEXT, NUMBER 등등")
        private CustomDataType dataType;

        @Schema(description = "필드 적용할 카테고리", example = "RESERVATION")
        private ServiceCategory category;
    }


    @Getter
    @Builder
    @Schema(description = "카테고리 필드 단일 조회 응답 DTO")
    public static class CategoryFieldDetailRes {

        @Schema(description = "필드 이름", example = "인원수")
        private String fieldName;

        @Schema(description = "필드 설명", example = "수용 가능한 인원 수를 입력해주세요.")
        private String description;

        @Schema(description = "데이터 타입", example = "TEXT, NUMBER 등등")
        private CustomDataType dataType;

        @Schema(description = "필드 적용할 카테고리", example = "RESERVATION")
        private ServiceCategory category;
    }


    @Getter
    @Builder
    @Schema(description = "카테고리 필드 목록 조회 응답 DTO")
    public static class CategoryFieldListRes {

        @Schema(description = "카테고리 필드 목록")
        private List<CategoryFieldDetailRes> categoryFields;
    }

}
