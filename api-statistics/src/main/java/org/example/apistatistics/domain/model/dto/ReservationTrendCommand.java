package org.example.apistatistics.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 추이 조회 요청 Command
 * - 리소스 그룹별 예약 트렌드 조회
 * - 특정 기간 동안의 예약 추이 분석
 */
@Getter
@Builder
@Schema(description = "예약 추이 조회 요청")
public class ReservationTrendCommand {

    @Schema(description = "조회할 리소스 그룹 ID 리스트", example = "[1, 2, 3]")
    private List<Long> groupIds;

    @Schema(description = "조회 시작 일시", example = "2025-01-01T00:00:00")
    private LocalDateTime from;

    @Schema(description = "조회 종료 일시", example = "2025-01-31T23:59:59")
    private LocalDateTime to;
}