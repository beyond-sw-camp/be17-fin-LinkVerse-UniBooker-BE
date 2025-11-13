package org.example.apiresource.domain.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.base.BaseEntity;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class ResourceTimeSlotExceptions extends BaseEntity {

    // 리소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resource;

    // 날짜
    @Column(nullable = false)
    private LocalDate date;

    // 시작 시간
    @Column(nullable = true )
    private LocalTime startTime;

    // 종료 시간
    @Column(nullable = true)
    private LocalTime endTime;

    // 휴무 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isClosed = true;

    // 비고
    private String note;

    public void setResources(Resources resources) {
        this.resource = resources;
    }
}