package org.example.unibooker.domain.activity.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.example.unibooker.common.BaseEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_events")
public class UserActivityEvent extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "이벤트를 발생시킨 유저")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @Schema(description = "서비스 제공 회사")
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    @Schema(description = "이벤트 타입", example = "RESERVATION")
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    @Schema(description = "이벤트 대상 타입", example = "RESOURCE")
    private TargetType targetType;

    @Column(name = "target_id")
    @Schema(description = "이벤트 대상 ID", example = "101")
    private Long targetId;

    @Column(name = "event_metadata")
    @Schema(description = "추가 메타데이터", example = "{\"browser\":\"Chrome\"}")
    private String eventMetadata;

    @Column(name = "event_time", nullable = false)
    @Schema(description = "이벤트 발생 시각", example = "2025-10-02T15:00:00")
    private LocalDateTime eventTime;

    public enum EventType {
        RESOURCE_DETAIL_VIEW,
        RESERVATION,
        CANCELLATION
    }

    public enum TargetType {
        RESOURCE,
        RESERVATION,
        RESOURCE_GROUP
    }
}
