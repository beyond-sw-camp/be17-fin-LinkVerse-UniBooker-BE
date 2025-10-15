package org.example.unibooker.domain.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.example.unibooker.common.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "resource_statistics")
public class ResourceStatistics extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    @Schema(description = "통계를 수집한 리소스")
    private Resource resource;

    @Column(name = "stat_start_date")
    @Schema(description = "통계 시작 날짜", example = "2025-10-01T00:00:00")
    private LocalDateTime statStartDate;

    @Column(name = "stat_date")
    @Schema(description = "통계 대상 날짜(YYYYMMDD 형태)", example = "20251002")
    private Integer statDate;

    @Column(name = "total_reservations")
    @Schema(description = "총 예약 수", example = "100")
    private Integer totalReservations;

    @Column(name = "successful_reservations")
    @Schema(description = "성공적으로 완료된 예약 수", example = "90")
    private Integer successfulReservations;

    @Column(name = "cancelled_reservations")
    @Schema(description = "취소된 예약 수", example = "5")
    private Integer cancelledReservations;

    @Column(name = "duplicate_reservations")
    @Schema(description = "중복 예약 수", example = "2")
    private Integer duplicateReservations;

    @Column(name = "utilization_rate")
    @Schema(description = "이용률 (0~1)", example = "0.9")
    private Float utilizationRate;

    @Column(name = "total_views")
    @Schema(description = "리소스 조회 수", example = "150")
    private Integer totalViews;

    @Column(name = "peak_hour")
    @Schema(description = "피크 시간 (24시간 기준)", example = "14")
    private Integer peakHour;

    @Column(name = "peak_reservations")
    @Schema(description = "피크 시간의 예약 수", example = "20")
    private Integer peakReservations;
}
