package org.example.apireservation.adapter.out;

import jakarta.persistence.LockModeType;
import org.example.apireservation.domain.model.Gender;
import org.example.apireservation.domain.model.entity.Reservations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface ReservationRepository extends JpaRepository<Reservations, Long> {

    // ========================== 사용자의 모든 예약 조회 ==========================
    List<Reservations> findAllByUserId(Long userId);

    // 삭제되지 않은 예약 조회
    Optional<Reservations> findByIdAndDeletedAtIsNull(Long reservationId);


    /** 사용자의 중복 예약 존재 하는지 조회 */
    // ========================== 사용자의 중복 예약 존재 하는지 조회 - 예약형, 신청형 ==========================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.userId = :userId AND r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservation(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // ========================== 사용자의 중복 예약 존재 하는지 조회 - 좌석형 ==========================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.userId = :userId AND r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.row = :row AND r.col = :col AND r.deletedAt IS NULL
    """)
    List<Reservations> findDuplicatedReservationSeat(Long userId, Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);


    /** 선택한 일시 예약 조회 */
    // ========================== 선택한 일시 예약 조회 - 예약형 ==========================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.deletedAt IS NULL
    """)
    List<Reservations> countByReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate);

    // ========================== 선택한 일시 예약 조회 - 좌석형 ==========================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Reservations r
        WHERE r.resourceId = :resourceId AND (r.startDate < :endDate AND r.endDate > :startDate) AND r.row = :row AND r.col = :col AND r.deletedAt IS NULL
    """)
    List<Reservations> countBySeatReservation(Long resourceId, LocalDateTime startDate, LocalDateTime endDate, Integer row, Integer col);

    // ========================== 선택한 일시 예약 조회 - 신청형 ==========================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Reservations> countByResourceIdAndDeletedAtIsNull(Long resourceId);

/*
    // ========================== 리소스 그룹의 예약 목록 찾기 ==========================
    @Query("SELECT r FROM Reservations r JOIN r.resources rs JOIN rs.resourceGroup rg WHERE rs.id = :resourceGroupId")
    List<Reservations> findAllByResourceGroupIdWithReservation(Long resourceGroupId);
*/

    // ========================== 특정 리소스의 예약 목록 조회 ==========================
    List<Reservations> findAllByResourceId(Long resources_id);

    // ========================== 특정 리소스의 예약 목록 조회 (특정 날짜) ==========================
    List<Reservations> findAllByResourceIdAndStartDateBetween(Long resources_id, LocalDateTime startDate, LocalDateTime endDate);

    // ========================== 특정 기업의 모든 예약 수 카운트 ==========================
    Integer countByCompanyId(Long companyId);

    // ========================== 특정 기간 동안의 리소스 그룹별 예약수 ==========================
    @Query("SELECT DATE(r.startDate), r.resourceGroupId, COUNT(r) " +
            "FROM Reservations r " +
            "WHERE r.resourceGroupId = :resourceGroupId AND r.startDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(r.startDate), r.companyId " +
            "ORDER BY DATE(r.startDate) ASC")
    List<Object[]> countReservationByGroupAndDate(Long resourceGroupId, LocalDateTime startDate, LocalDateTime endDate);

/*
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

    // ========================== 특정 리소스 그룹의 모든 예약 수 (예약 + 취소) ==========================
    Integer countByResourceGroupId(Long resourceGroupId);

    // ========================== 누적 예약 수 ==========================
    Integer countByResourceGroupIdAndDeletedAtIsNull(Long resourceGroupId);

    // ========================== 누적 취소 수 ==========================
    Integer countByResourceGroupIdAndDeletedAtIsNotNull(Long resourceGroupId);

    // ========================== 리소스 그룹에 속하는 리소스 수 ==========================
    @Query("SELECT r.resourceId, COUNT(r) " +
            "FROM Reservations r " +
            "WHERE r.resourceGroupId = :resourceGroupId " +
            "AND r.deletedAt IS NULL " +
            "AND r.createdAt >= :oneMonthAgo " +
            "GROUP BY r.resourceId")
    List<Object[]> getServicePerformanceCount(Long resourceGroupId, LocalDateTime oneMonthAgo);

    // ========================== 리소스 그룹에 속하는 사용자 (중복제거) ==========================
    @Query("SELECT COUNT(DIStINCT r.userId) FROM Reservations r WHERE r.resourceGroupId=:resourceGorupId")
    Integer getReservationUserCount(Long resourceGroupId);

    // ========================== 성별 ==========================
    @Query("SELECT u.gender, COUNT(r) " +
            "FROM Reservations r " +
            "JOIN Users u " +
            "WHERE r.resourceGroupId = :resourceGroupId")
    List<Object[]> countByGenderReservation(Long resourceGroupId);

    // ========================== 나이대 ==========================
    // TODO : query 노란 오류 수정 필요
    @Query(value =
            "SELECT CASE " +
            "WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.birth_date, '%Y-%m-%d'), CURDATE()) BETWEEN 10 AND 19 THEN 10 " +
            "WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.birth_date, '%Y-%m-%d'), CURDATE()) BETWEEN 20 AND 29 THEN 20 " +
            "WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.birth_date, '%Y-%m-%d'), CURDATE()) BETWEEN 30 AND 39 THEN 30 " +
            "WHEN TIMESTAMPDIFF(YEAR, STR_TO_DATE(u.birth_date, '%Y-%m-%d'), CURDATE()) >= 40 THEN 40 " +
            "END AS age_group, COUNT(*) AS cnt " +
            "FROM reservations r " +
            "JOIN users u ON r.user_id = u.id " +
            "WHERE r.resource_group_id = :resourceGroupId " +
            "GROUP BY gender, age_group " +
            "ORDER BY gender, age_group",
            nativeQuery = true)
    List<Object[]> countByAgeReservation(Long resourceGroupId);

    // ========================== 리소스 그룹에 속하는 시간대 별 예약수 ==========================
    @Query("SELECT HOUR(r.startDate), COUNT(*) "+
            "FROM Reservations r " +
            "WHERE DATE(r.startDate) = CURDATE() " +
            "GROUP BY HOUR(r.startDate) " +
            "ORDER BY HOUR(r.startDate)")
    List<Object[]> getTimeSlotReservationCount(Long resourceGroupId);
}
