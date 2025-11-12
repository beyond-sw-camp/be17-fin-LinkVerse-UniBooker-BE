package org.example.apireservation.adapter.out;

import jakarta.persistence.LockModeType;
import org.example.apireservation.domain.model.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.*;

public interface ReservationRepository extends JpaRepository<Reservations, Long> {

    // 사용자의 모든 예약 조회
    List<Reservations> findAllByUsersId(Long userId);

    // 삭제되지 않은 예약 조회
    Optional<Reservations> findByIdAndDeletedAtIsNull(Long reservationId);


    /** 사용자의 중복 예약 존재 하는지 조회 */
    // 사용자의 중복 예약 존재 하는지 조회 - 예약형, 신청형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.userId = :userId AND r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservation(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 사용자의 중복 예약 존재 하는지 조회 - 좌석형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.userId = :userId AND r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.row = :row AND r.col = :col AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservationSeat(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);


    /** 선택한 일시 예약 조회 */
    // 선택한 일시 예약 조회 - 예약형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
    """)
    List<Reservations> countByReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 선택한 일시 예약 조회 - 좌석형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.row = :row AND r.col = :col AND r.deletedAt IS NULL
    """)
    List<Reservations> countBySeatReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);

    // 선택한 일시 예약 조회 - 신청형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservations> countByResourcesIdAndDeletedAtIsNull(Long resourceId);

/*
    // 리소스 그룹의 예약 목록 찾기
    @Query("SELECT r FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.id = :resourceGroupId")
    List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId);

    // 특정 기업의 모든 예약 수 카운트
    @Query("SELECT COUNT(r) FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.company.id = :companyId")
    int countByCompanyId(Long companyId);

    // 특정 리소스 그룹의 예약 수
    @Query("SELECT COUNT(r) FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.id = :resourceGroupId")
    int countByResourceGroupId(Long resourceGroupId);
*/

    // 특정 리소스의 예약 목록 조회
    List<Reservations> findAllByResourcesId(Long resources_id);

/*
    // 특정 리소스의 예약 목록 조회 (특정 날짜)
    List<Reservations> findAllByResourcesIdAndStartDateBetween(Long resources_id, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기간 동안의 리소스 그룹별 예약수
    @Query("SELECT DATE(r.startDate), rg.name, COUNT(r) " +
            "FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.company.id = :companyId AND r.startDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(r.startDate), rg.name " +
            "ORDER BY DATE(r.startDate) ASC")
    List<Object[]> countReservationsByGroupAndDate(Long companyId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
    SELECT COUNT(r.id)
    FROM Reservations r
    JOIN r.resources res
    WHERE res.id = :resourceId
      AND r.status = 'CONFIRMED'
      AND r.startDate < :endDate
      AND r.endDate   > :startDate
    GROUP BY res.id, res.name
""")
    int countConfirmedByResourceAndRange(
            Long resourceId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
*/
}
