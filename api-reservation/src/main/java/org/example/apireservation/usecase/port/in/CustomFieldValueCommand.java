package org.example.apireservation.usecase.port.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Adapter.in 으로 들어오는 예약 요청시
 * 유저가 입력해야하는 커스텀 필드 값 DTO */
@Getter
@Schema(description = "커스텀 필드 값 생성 정보")
public class CustomFieldValueCommand {

    @Schema(description = "필드 ID", example = "1")
    private Long customFieldId;

    @Schema(description = "입력 값", example = "회의실 101")
    private List<String> values;
}
