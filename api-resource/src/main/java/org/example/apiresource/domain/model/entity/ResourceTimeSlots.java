package org.example.apiresource.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.apiresource.domain.model.DayOfWeek;
import org.example.common.base.BaseEntity;
import org.hibernate.annotations.Where;

import java.time.LocalTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class ResourceTimeSlots extends BaseEntity {

    // 리소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resources;

    // 시작 시간
    @Column(nullable = false)
    private LocalTime startTime;

    // 종료 시간
    @Column(nullable = false)
    private LocalTime endTime;

    // 요일
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    // 운영 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @PrePersist
    @PreUpdate
    private void upperCaseDayOfWeek() {
        if (this.dayOfWeek != null) {
            this.dayOfWeek = DayOfWeek.valueOf(this.dayOfWeek.name().toUpperCase());
        }
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}