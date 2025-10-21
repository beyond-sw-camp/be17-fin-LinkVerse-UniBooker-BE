package org.example.unibooker.domain.resource.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceTimeSlotExceptions extends BaseEntity {

    // 리소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resources;

    @Column(nullable = false)
    private LocalDate date; // 날짜

    @Column(nullable = false)
    private LocalTime startTime; // 시작 시간

    @Column(nullable = false)
    private LocalTime endTime; // 종료 시간

    @Column(nullable = false)
    @Builder.Default
    private Boolean isClosed = true; // 휴무 여부

    private Character note; // 비고
}