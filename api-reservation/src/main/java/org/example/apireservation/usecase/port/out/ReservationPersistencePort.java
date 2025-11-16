package org.example.apireservation.usecase.port.out;

import org.example.apireservation.domain.model.Gender;
import org.example.apireservation.domain.model.dto.ReservationTrendDto;
import org.example.apireservation.domain.model.entity.Reservations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationPersistencePort {

    // 사용자의 모든 예약 조회
    List<Reservations> findAllByUserId(Long userId);

    // 삭제되지 않은 예약 조회
    Optional<Reservations> findByIdAndDeletedAtIsNull(Long reservationId);

    // 예약 상세 조회
    Optional<Reservations> findById(Long reservationId);

    // 예약하기
    Reservations save(Reservations reservation);

    // 사용자의 중복 예약 존재하는지 조회 - 예약형, 신청형
    List<Reservations> findDuplicatedReservation(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 사용자의 중복 예약 존재하는지 조회 - 좌석형
    List<Reservations> findDuplicatedReservationSeat(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);

    // 선택한 일시 예약 조회 - 좌석형
    List<Reservations> countBySeatReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);

    // 선택한 일시 예약 조회 - 예약형
    List<Reservations> countByReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 선택한 일시 예약 조회 - 신청형
    List<Reservations> countByResourceIdAndDeletedAtIsNull(Long resourceId);

    // 리소스 그룹의 예약 목록 찾기
    List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId);

/*
    // 리소스의 예약 목록 찾기
    List<Reservations> findAllByResourceIdWithReservation(Long resourceId);
*/

    // 특정 리소스 그룹의 예약 수
    Integer countByResourceGroupId(Long resourceGroupId);

    // 특정 리소스의 예약 목록 조회
    List<Reservations> findAllByResourceId(Long resourceId);

    // 특정 리소스의 예약 목록 조회 (특정 날짜)
    List<Reservations> findAllByResourceIdAndStartDateBetween(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기업의 모든 예약 수 카운트
    Integer countByCompanyId(Long companyId);

    // 특정 기간 동안의 리소스 그룹별 예약수 카운트
    List<Object[]> countReservationByGroupAndDate(Long resourceGroupId, LocalDateTime startDate, LocalDateTime endDate);

    // 누적 예약수
    Integer getCumReservationCount(Long resourceGroupId);

    // 누적 취소수
    Integer getCumCancelCount(Long resourceGroupId);

    // 리소스 그룹에 속하는 리소스 수
    List<Object[]> getServicePerformanceCount(Long resourceGroupId, LocalDateTime oneMonthAgo);

    // 리소스 그룹에 속하는 사용자
    Integer getReservationUserCount(Long resourceGroupId);

    // 성별
    List<Object[]> getGenderReservationCount(Long resourceGroupId);

    // 나이대
    List<Object[]> getAgeReservationCount(Long resourceGroupId);

    // 리소스 그룹에 속하는 시간대 별 예약 수
    List<Object[]> getTimeSlotReservationCount(Long resourceGroupId);

    Integer countConfirmedByResourceAndRange(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);
}
