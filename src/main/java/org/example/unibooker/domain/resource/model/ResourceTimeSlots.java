package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;

import java.time.LocalTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}