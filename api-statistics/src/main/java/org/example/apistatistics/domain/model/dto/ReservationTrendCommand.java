package org.example.apistatistics.domain.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 추이 조회 요청 Command
 */
@Getter
@Builder
public class ReservationTrendCommand {
    private List<Long> groupIds;   // 리소스 그룹 ID 리스트
    private LocalDateTime from;    // 조회 시작일
    private LocalDateTime to;      // 조회 종료일
}