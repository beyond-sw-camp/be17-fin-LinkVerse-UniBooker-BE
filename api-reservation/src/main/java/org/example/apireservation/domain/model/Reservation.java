package org.example.apireservation.domain.model;

import lombok.*;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.domain.service.ReservationService;
import org.example.apireservation.usecase.port.in.ReservationCommand;

import java.time.LocalDateTime;

/**
 * Command와 Entity 사이 중간 역할하는 DTO
 * 핵심 비즈니스 데이터와 규칙 */
@Getter
@Builder
public class Reservation {
    /* 예약 관련 */
    private Long id;                                // 예약 번호
    private ReservationStatus status;               // 예약 상태
    private Integer headCount;                      // 인원수
    private Integer row;                            // 좌석 행
    private Integer col;                            // 좌석 열
    private LocalDateTime startDate;                // 예약 시작 일시
    private LocalDateTime endDate;                  // 예약 종료 일시
    private LocalDateTime createdAt;                // 생성일
    private LocalDateTime updatedAt;                // 수정일
    private LocalDateTime deletedAt;                // 삭제일

    /* user 관련 */
    private Long userId;                            // 사용자 ID
    private String userName;                        // 사용자 이름
    private String email;                           // 사용자 이메일

    /* 리소스 관련*/
    private Long resourceId;                        // 리소스 ID
    private String resourceName;                    // 리소스명
    private String resourceImage;                   // 리소스 이미지

    /* 리소스 그룹 관련 */
    private String resourceGroupName;               // 리소스 그룹 명
    private ServiceCategory serviceCategory;        // 서비스 카테고리


    // ========================== Command -> Domain (예약하기) ==========================
    public static Reservation toDomain(ReservationCommand dto, Long resourceId, Long userId, ReservationService reservationService) {

        // 유저 및 리소스 유효성 검증
        User user = reservationService.validateUser(userId);
        Resource resource = reservationService.validateResource(resourceId);

        // 예약일 날짜 변환
        LocalDateTime[] dates = reservationService.transDate(resource, dto);

        // 중복 예약 체크 (사용자 입장)
        reservationService.duplicatedReservationCheck(resource, user, dates, dto);

        // 정원 초과 체크 (리소스 입장)
        reservationService.overCapacityCheck(resource, dates, dto);

        return Reservation.builder()
                .userId(userId)
                .userName(user.getUserName())
                .email(user.getEmail())
                .resourceId(resourceId)
                .resourceName(resource.getName())
                .resourceImage(resource.getResourceImage())
                .resourceGroupName(resource.getResourceGroupName())
                .serviceCategory(resource.getCategory())
                .startDate(dates[0])
                .endDate(dates[1])
                .headCount(dto.getHeadCount())
                .row(dto.getRow())
                .col(dto.getCol())
                .build();
    }

    // ========================== Command -> Domain (상세조회) ==========================
    public static Reservation toDomain(Reservations entity, Long resourceId, Long userId, ReservationService reservationService) {

        // 유저 및 리소스 유효성 검증
        User user = reservationService.validateUser(userId);
        Resource resource = reservationService.validateResource(resourceId);

        return Reservation.builder()
                .id(entity.getId())
                .userId(userId)
                .userName(user.getUserName())
                .email(user.getEmail())
                .resourceId(resource.getId())
                .resourceName(resource.getName())
                .resourceImage(resource.getResourceImage())
                .resourceGroupName(resource.getResourceGroupName())
                .serviceCategory(resource.getCategory())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .headCount(entity.getAttendeeCount())
                .row(entity.getRow())
                .col(entity.getCol())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
