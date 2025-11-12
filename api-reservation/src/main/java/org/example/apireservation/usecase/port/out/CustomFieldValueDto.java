package org.example.apireservation.usecase.port.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 유저 커스텀 필드 값 정보
 * 응답할 데이터를 담은 DTO */
@Getter
@Builder
public class CustomFieldValueDto {

    @Schema(description = "필드 ID", example = "1")
    private Long customFieldId;

    @Schema(description = "필드 이름", example = "회의실 이름")
    private String fieldName;

    @Schema(description = "값 목록", example = "[\"101호 회의실\"]")
    private List<String> values;
}
