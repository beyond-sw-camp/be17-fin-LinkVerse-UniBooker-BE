package org.example.unibooker.domain.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.company.model.Company;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
public class AccessLogs extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "대기열에 있는 사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_group_id", nullable = true)
    @Schema(description = "접근하려는 서비스 그룹")
    private ResourceGroups resourceGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = true)
    @Schema(description = "접근하려는 서비스")
    private Resources resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @Schema(description = "서비스를 제공하는 기업")
    private Company company;

    @Column(name = "requested_at", nullable = false)
    @Schema(description = "접속 요청 시각", example = "2025-10-02T15:00:00")
    private LocalDateTime requestedAt;

    @Column(name = "entered_at")
    @Schema(description = "접속한 시각", example = "2025-10-02T15:01:30")
    private LocalDateTime enteredAt;

    @Column(name = "cancelled_at")
    @Schema(description = "접속 취소 시각", example = "2025-10-02T15:05:00")
    private LocalDateTime cancelledAt;

}
