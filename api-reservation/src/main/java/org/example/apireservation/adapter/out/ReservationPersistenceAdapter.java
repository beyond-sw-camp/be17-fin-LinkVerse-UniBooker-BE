package org.example.apireservation.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.usecase.port.out.ReservationPersistencePort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/** DB 접근만 담당하는 구현체 */
@Component
@RequiredArgsConstructor
public class ReservationPersistenceAdapter implements ReservationPersistencePort {

    private final ReservationRepository reservationRepository;

    // ========================== 사용자의 모든 예약 조회 ==========================
    @Override
    public List<Reservations> findAllByUserId(Long userId) {
        return reservationRepository.findAllByUserId(userId);
    }

    // ========================== 삭제되지 않은 예약 조회 ==========================
    @Override
    public Optional<Reservations> findByIdAndDeletedAtIsNull(Long reservationId) {
        return reservationRepository.findByIdAndDeletedAtIsNull(reservationId);
    }

    // ========================== 예약 상세 조회 ==========================
    @Override
    public Optional<Reservations> findById(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    // ========================== 예약하기 ==========================
    @Override
    public Reservations save(Reservations reservation) {
        return reservationRepository.save(reservation);
    }

    // ========================== 사용자의 중복 예약 존재하는지 조회 - 예약형, 신청형 ==========================
    @Override
    public List<Reservations> findDuplicatedReservation(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findDuplicatedReservation(userId, resourceId, startDate, endDate);
    }

    // ========================== 사용자의 중복 예약 존재하는지 조회 - 좌석형 ==========================
    @Override
    public List<Reservations> findDuplicatedReservationSeat(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col) {
        return reservationRepository.findDuplicatedReservationSeat(userId, resourceId, startDate, endDate, row, col);
    }

    // ========================== 선택한 일시 예약 조회 - 좌석형 ==========================
    @Override
    public List<Reservations> countBySeatReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col) {
        return reservationRepository.countBySeatReservation(resourceId, startDate, endDate, row, col);
    }

    // ========================== 선택한 일시 예약 조회 - 예약형 ==========================
    @Override
    public List<Reservations> countByReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.countByReservation(resourceId, startDate, endDate);
    }

    // ========================== 선택한 일시 예약 조회 - 신청형 ==========================
    @Override
    public List<Reservations> countByResourceIdAndDeletedAtIsNull(Long resourceId) {
        return reservationRepository.countByResourceIdAndDeletedAtIsNull(resourceId);
    }

    // ========================== 리소스 그룹의 예약 목록 찾기 ==========================
    @Override
    public List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId) {
//        return reservationRepository.findAllByResourceGroupIdWithReservation(resourceGroupId);
        return null;
    }

/*
    // ========================== 리소스의 예약 목록 찾기 ==========================
    @Override
    public List<Reservations> findAllByResourceIdWithReservation(Long resourceId) {
        return reservationRepository.findAllByResourceIdWithReservation(resourceId);
    }
*/

    // ========================== 특정 기업의 모든 예약 수 카운트 ==========================
    @Override
    public Integer countByCompanyId(Long companyId) {
//        return reservationRepository.countByCompanyId(companyId);
        return null;
    }

    // ========================== 특정 리소스 그룹의 예약 수 ==========================
    @Override
    public Integer countByResourceGroupId(Long resourceGroupId) {
//        return reservationRepository.countByResourceGroupId(resourceGroupId);
        return null;
    }

    // ========================== 특정 리소스의 예약 목록 조회 ==========================
    @Override
    public List<Reservations> findAllByResourceId(Long resourceId) {
        return reservationRepository.findAllByResourceId(resourceId);
    }

    // ========================== 특정 리소스의 예약 목록 조회 (특정 날짜) ==========================
    @Override
    public List<Reservations> findAllByResourceIdAndStartDateBetween(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
//        return reservationRepository.findAllByResourceIdAndStartDateBetween(resourceId, startDate, endDate);
        return null;
    }

    // ========================== 특정 기간 동안의 리소스 그룹별 예약수 ==========================
    @Override
    public List<Object[]> countReservationsByGroupAndDate(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
//        return reservationRepository.countReservationsByGroupAndDate(companyId, startDate, endDate);
        return null;
    }

    // ==========================  ==========================
    @Override
    public Integer countConfirmedByResourceAndRange(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
//        return reservationRepository.countConfirmedByResourceAndRange(resourceId, startDate, endDate);
        return null;
    }
}
