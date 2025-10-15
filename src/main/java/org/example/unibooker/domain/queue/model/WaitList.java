package org.example.unibooker.domain.queue.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.example.unibooker.common.BaseEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist")
public class WaitList extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "대기열에 있는 사용자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_group_id", nullable = false)
    @Schema(description = "대기하는 리소스 그룹")
    private ResourceGroup resourceGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @Schema(description = "소속 회사")
    private Company company;

    @Column(name = "requested_at", nullable = false)
    @Schema(description = "접속 요청 시각", example = "2025-10-02T15:00:00")
    private LocalDateTime requestedAt;

    @Column(name = "entered_at")
    @Schema(description = "접속한 시각", example = "2025-10-02T15:01:30")
    private LocalDateTime allowedAt;

    @Column(name = "cancelled_at")
    @Schema(description = "접속 취소 시각", example = "2025-10-02T15:05:00")
    private LocalDateTime cancelledAt;

}
