package org.example.unibooker.domain.reservation.repository;

import jakarta.persistence.LockModeType;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        WHERE r.users.id = :userId AND r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservation(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // 사용자의 중복 예약 존재 하는지 조회 - 좌석형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.users.id = :userId AND r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.row = :row AND r.col = :col AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservationSeat(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);

    /** 선택한 일시 예약 조회 */
    // 선택한 일시 예약 조회 - 예약형
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.resources.id = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
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

    // 리소스의 예약 목록 찾기
    @Query("SELECT r FROM Reservations r LEFT JOIN r.resources rs WHERE rs.id = :resourceId")
    List<Reservations> findAllByResourceIdWithReservation(Long resourceId);

    // 리소스 그룹의 예약 목록 찾기
    @Query("SELECT r FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.id = :resourceGroupId")
    List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId);

    // 특정 기업의 모든 예약 수 카운트
    @Query("SELECT COUNT(r) FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.company.id = :companyId")
    int countByCompanyId(Long companyId);

    // 특정 리소스 그룹의 예약 수
    @Query("SELECT COUNT(r) FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rg.id = :resourceGroupId")
    int countByResourceGroupId(Long resourceGroupId);

    // 특정 리소스의 예약 목록 조회
    List<Reservations> findAllByResourcesId(Long resources_id);

    // 특정 리소스의 예약 목록 조회 (특정 날짜)
    List<Reservations> findAllByResourcesIdAndStartDateBetween(Long resources_id, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기간 동안의 리소스 그룹별 예약수
    @Query("SELECT DATE(r.createdAt), rg.name, COUNT(r) " +
            "FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.company.id = :companyId AND r.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(r.createdAt), rg.name " +
            "ORDER BY DATE(r.createdAt) ASC")
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

    /** 리소스 그룹별 취소된 예약 수 */
    @Query("SELECT COUNT(r) FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.id = :resourceGroupId AND r.status = 'CANCELLED'")
    int countCancelledByResourceGroupId(Long resourceGroupId);

    /** 리소스 그룹별 고유 사용자 수 */
    @Query("SELECT COUNT(DISTINCT r.users.id) FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.id = :resourceGroupId")
    int countDistinctUsersByResourceGroupId(Long resourceGroupId);

    /** 기간 내 리소스별 예약 수 */
    @Query("SELECT res.name, COUNT(r) FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.id = :resourceGroupId AND r.createdAt >= :startDate " +
            "GROUP BY res.id, res.name")
    List<Object[]> countByResourceInPeriod(Long resourceGroupId, LocalDateTime startDate);

    /** 시간별 예약 수 */
    @Query("SELECT HOUR(r.createdAt), COUNT(r) FROM Reservations r " +
            "JOIN r.resources res " +
            "JOIN res.resourceGroup rg " +
            "WHERE rg.id = :resourceGroupId " +
            "AND r.createdAt >= :startDate AND r.createdAt < :endDate " +
            "GROUP BY HOUR(r.createdAt)")
    List<Object[]> countByHour(Long resourceGroupId, LocalDateTime startDate, LocalDateTime endDate);

}
