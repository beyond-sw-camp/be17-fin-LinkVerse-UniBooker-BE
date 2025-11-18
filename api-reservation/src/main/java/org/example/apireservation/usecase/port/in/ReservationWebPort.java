package org.example.apireservation.usecase.port.in;

import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;
import org.example.apireservation.domain.model.dto.ReservationTrendDto;
import org.example.apireservation.domain.model.dto.ServiceGroupDashBoardDto;
import org.example.common.model.UserRole;

import java.time.LocalDateTime;
import java.util.*;

public interface ReservationWebPort {

    // 예약 요청
    ReservationDetailDto.Response reserve(ReservationCommand dto, Long resourceId, Long userId, Long companyId);

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

    // 누적 예약 수
    Integer getCumReservationCount(Long resourceGroupId);

    // 누적 취소 수
    Integer getCumCancelCount(Long resourceGroupId);

    // 서비스별 성과 (리소스 ID, 총 예약 수)
    List<ServiceGroupDashBoardDto.ServicePerformanceCount> getServicePerformanceCount(Long resourceGroupId);

    // 이용자 수 (전체 이용자 수, 에약한 사람 수)
    ServiceGroupDashBoardDto.VisitorCount getVisitorCount(Long resourceGroupId, Long companyId, UserRole UserRole);

    // 사용자 특성별 이용 - 성별
    List<ServiceGroupDashBoardDto.GenderReservationCount> getGenderReservationCount(Long resourceGroupId);

    // 사용자 특성별 이용 - 나이
    List<ServiceGroupDashBoardDto.AgeReservationCount> getAgeReservationCount(Long resourceGroupId);

    // 시간대별 예약 현황
    List<ServiceGroupDashBoardDto.TimeSlotReservationCount> getTimeSlotReservationCount(Long resourceGroupId);

    // 리소스 그룹별 예약 수
    List<ServiceGroupDashBoardDto.GroupReservationCountResponse> getCumReservationCountsByGroupResources(List<Long> groupIds);
}