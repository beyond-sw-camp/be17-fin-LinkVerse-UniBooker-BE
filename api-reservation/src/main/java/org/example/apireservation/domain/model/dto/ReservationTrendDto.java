package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 리소스 그룹별 예약 수를 담은 DTO
 * 변환 로직은 ReservationMappter에 있음 */
@Getter
@Builder
public class ReservationTrendDto {
    private LocalDate date;
    private Long groupId;
    private Integer count;
}
