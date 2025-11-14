package org.example.apireservation.usecase.port.in;

import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;
import org.example.apireservation.domain.model.dto.ReservationTrendDto;
import org.example.common.user.AuthDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationWebPort {

    // 예약 요청
    ReservationDetailDto.Response reserve(ReservationCommand dto, Long resourceId, AuthDto authUser);

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

    // 특정 기업의 전체 예약 수 조회
    Integer getAllReservationCountsByCompany(Long companyId);

    // 특정 기간 동안의 리소스 그룹별 예약 수 조회
    List<ReservationTrendDto> getReservationCountsByGroupResources(ReservationTrendCommand dto);
}