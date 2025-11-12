package org.example.apireservation.adapter.out;

import jakarta.persistence.*;
import lombok.*;
import org.example.apireservation.domain.model.ReservationStatus;
import org.example.common.base.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservations extends BaseEntity {

    // 사용자 키
    @Column(nullable = false)
    private Long userId;

    // 리소스 키
    @Column(nullable = false)
    private Long resourceId;

    // 생성자
    private Long createdBy;

    // 예약 상태
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Integer attendeeCount;      // 참석 인원
    private LocalDateTime startDate;    // 시작 일시
    private LocalDateTime endDate;      // 종료 일시
    private Integer row;                // 좌석 행
    private Integer col;                // 좌석 열

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        softDelete();
    }
}
