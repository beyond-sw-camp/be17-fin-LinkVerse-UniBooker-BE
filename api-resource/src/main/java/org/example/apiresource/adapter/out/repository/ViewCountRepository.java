package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.entity.ResourceGroupViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ViewCountRepository extends JpaRepository<ResourceGroupViewCount, Long> {

    @Query("SELECT COALESCE(SUM(v.viewCount), 0) " +
            "FROM ResourceGroupViewCount v " +
            "WHERE v.resourceGroup.id = :resourceGroupId " +
            "AND v.createdAt >= :startOfDay " +
            "AND v.createdAt < :endOfDay")
    long sumViewsByResourceGroupIdAndDate(Long resourceGroupId,
                                          LocalDateTime startOfDay,
                                          LocalDateTime endOfDay);

    @Query("SELECT HOUR(v.createdAt) AS hour, COUNT(v.id) AS viewCount " +
            "FROM ResourceGroupViewCount v " +
            "WHERE v.resourceGroup.id = :resourceGroupId " +
            "AND v.createdAt >= :startOfDay " +
            "AND v.createdAt < :endOfDay " +
            "GROUP BY HOUR(v.createdAt) " +
            "ORDER BY HOUR(v.createdAt) ASC")
    List<Object[]> getTodayHourlyViews(
            @Param("resourceGroupId") Long resourceGroupId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
