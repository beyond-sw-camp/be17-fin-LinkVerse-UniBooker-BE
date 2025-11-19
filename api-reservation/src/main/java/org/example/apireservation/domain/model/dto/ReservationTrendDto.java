package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 리소스 그룹별 예약 트렌드 DTO
 * - 일별 예약 수 추이 제공
 * - 변환 로직은 ReservationMapper에 있음
 */
@Getter
@Builder
@Schema(description = "리소스 그룹별 예약 트렌드")
public class ReservationTrendDto {

    @Schema(description = "날짜", example = "2025-01-15")
    private LocalDate date;

    @Schema(description = "리소스 그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "예약 수", example = "10")
    private Integer count;
}