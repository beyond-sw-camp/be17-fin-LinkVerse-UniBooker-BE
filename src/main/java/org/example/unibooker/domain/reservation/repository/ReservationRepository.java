package org.example.unibooker.domain.reservation.repository;

import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservations, Long> {
    List<Reservations> findAllByUsersId(Long userId);

    // TODO : 삭제되지 않고, 리소스 상태가 CLOSED가 아닌 예약
    // 사용자의 중복 예약 존재 여부
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservations r
        WHERE r.users.id = :userId AND r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate)
    """)
    Boolean existsByUserIdAndResourceIdAndStartDateBetween(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 예약 카테고리 - 해당 시간대 예약 존재 여부
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservations r
        WHERE r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate)
    """)
    Boolean existsByResourceIdAndTimeRange(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 좌석 카테고리 - 날짜 단위 예약 수 카운트
    @Query("""
        SELECT COUNT(r)
        FROM Reservations r
        WHERE r.resources.id = :resourceId AND DATE(r.startDate) = :date
    """)
    Integer countByResourceIdAndDate(Long resourceId, LocalDate date);

    // 리소스의 예약 목록 찾기
    @Query("SELECT r FROM Reservations r LEFT JOIN r.resources rs WHERE rs.id = :resourceId")
    List<Reservations> findAllByResourceIdWithReservation(Long resourceId);

    // 리소스 그룹의 예약 목록 찾기
    @Query("SELECT r FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.id = :resourceGroupId")
    List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId);
}
