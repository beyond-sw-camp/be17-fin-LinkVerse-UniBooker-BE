package org.example.apireservation.usecase.port.in;

import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;

import java.time.LocalDateTime;

public interface ReservationWebPort {

    // 예약 요청
    ReservationDetailDto.Response reserve(ReservationCommand dto, Long resourceId, Long userId);

    // 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스 그룹"의 목록
    ReservationListDto.ResponseList getAdminReservations(Long resourceGroupId);

    // 특정 서비스의 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자
    ReservationListDto.ResponseList getResourceReservations(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 예약 목록 조회- 일반 사용자
    ReservationListDto.UserResponseList getUserReservations(Long userId);

    // 예약 상세 조회
    ReservationDetailDto.Response getReservationDetail(Long reservationId);

    // 예약 취소
    void cancel(Long reservationId, Long userId);
}