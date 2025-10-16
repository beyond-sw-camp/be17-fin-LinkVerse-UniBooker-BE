package org.example.unibooker.domain.reservation.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservations extends BaseEntity {

    // 사용자 키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    // 리소스 키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resources;

    // 생성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Users createdBy;

    // 예약 상태
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Integer attendeeCount; // 참석 인원
    private LocalDateTime startTime; // 시작 일시
    private LocalDateTime endTime; // 종료 일시
}
