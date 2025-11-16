package org.example.apireservation.usecase.port.in;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/** Adapter.in 으로 들어오는 리소스 그룹별 예약 수 요청 DTO */
@Getter
@Builder
public class ReservationTrendCommand {
    private List<Long> groupIds;   // 리소스 그룹 ID 리스트
    private LocalDateTime from;    // 조회 시작일
    private LocalDateTime to;      // 조회 종료일
}
