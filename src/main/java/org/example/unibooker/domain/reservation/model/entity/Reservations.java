package org.example.unibooker.domain.reservation.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime createdAt; // 생성 일시
    private LocalDateTime updatedAt; // 수정 일시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users; // 사용자 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resources resources; // 리소스 키

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 예약 상태

    private Integer attendeeCount; // 참석 인원
    private LocalDateTime startTime; // 시작 일시
    private LocalDateTime endTime; // 종료 일시
}
