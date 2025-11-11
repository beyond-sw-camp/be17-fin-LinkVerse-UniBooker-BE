package org.example.apireservation.domain.model;

import lombok.*;
import org.example.apireservation.adapter.out.Reservations;
import org.example.apireservation.domain.service.ReservationService;
import org.example.apireservation.usecase.port.in.ReservationCommand;

import java.time.LocalDateTime;

/**
 * Command와 Entity 사이 중간 역할하는 DTO
 * 핵심 비즈니스 데이터와 규칙 */
@Getter
@Builder
public class Reservation {
    private Long id;
    private Long userId;
    private Long resourceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer headCount;
    private Integer row;
    private Integer col;


    // ========================== Command -> Domain ==========================
    public static Reservation toDomain(ReservationCommand dto, Long resourceId, Long userId, ReservationService reservationService) {

        // 유저 및 리소스 유효성 검증
        reservationService.validateUser(userId);
        reservationService.validateResource(resourceId);

        // 예약일 날짜 변환
        LocalDateTime[] dates = reservationService.transDate(resourceId, dto);

        // 중복 예약 체크 (사용자 입장)
        reservationService.duplicatedReservationCheck(resourceId, userId, dates, dto);

        // 정원 초과 체크 (리소스 입장)
        reservationService.overCapacityCheck(resourceId, dates, dto);

        return Reservation.builder()
                .userId(userId)
                .resourceId(resourceId)
                .startDate(dates[0])
                .endDate(dates[1])
                .headCount(dto.getHeadCount())
                .row(dto.getRow())
                .col(dto.getCol())
                .build();
    }


    // ========================== Domain -> Entity ==========================
    public Reservations toEntity() {
        return Reservations.builder()
                .userId(userId)
                .resourceId(resourceId)
                .createdBy(userId)
                .status(ReservationStatus.CONFIRMED)
                .attendeeCount(headCount)
                .startDate(startDate)
                .endDate(endDate)
                .row(row)
                .col(col)
                .build();
    }
}
