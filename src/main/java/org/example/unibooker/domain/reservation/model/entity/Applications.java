package org.example.unibooker.domain.reservation.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.unibooker.common.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Applications extends BaseEntity {

    @JoinColumn(name = "user_id")
    private Users users; // 사용자 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resources; // 리소스 키

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 예약 상태
}
