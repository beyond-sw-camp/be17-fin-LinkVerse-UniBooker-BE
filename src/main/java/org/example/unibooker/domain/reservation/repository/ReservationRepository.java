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
    List<Reservations> findAllByUsers_Id(Long userId);

    // 사용자의 중복 예약 존재 여부
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservations r
        WHERE r.users.id = :userId AND r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate)
    """)
    Boolean existsByUserIdAndResourceIdAndStartDateBetween(
            Long userId, Long resourceId,
            LocalDateTime startDate, LocalDateTime endDate
    );

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
}
