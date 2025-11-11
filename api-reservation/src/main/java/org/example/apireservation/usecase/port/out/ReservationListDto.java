package org.example.apireservation.usecase.port.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.apireservation.adapter.out.Reservations;
import org.example.apireservation.domain.model.ReservationStatus;
import org.example.apireservation.domain.model.ServiceCategory;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 예약 목록
 * 응답할 데이터를 담은 DTO */
public class ReservationListDto {

    // =============== 플랫폼 관리자 및 기업 관리자 용 ===============
    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 응답 정보")
    public static class ResponseList {
        private List<Object> list;

        public static ResponseList from(List<Reservations> entities, ServiceCategory serviceCategory) {
            return switch(serviceCategory) {
                case RESERVATION ->
                        ResponseList.builder()
                                .list(entities.stream().map(ReservationResponseListInfo::from).collect(Collectors.toList()))
                                .build();
                case SEAT ->
                        ResponseList.builder()
                                .list(entities.stream().map(SeatResponseListInfo::from).collect(Collectors.toList()))
                                .build();
                case EVENT ->
                        ResponseList.builder()
                                .list(entities.stream().map(EventResponseListInfo::from).collect(Collectors.toList()))
                                .build();
                default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
            };
        }
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [예약형] 단일 응답 정보")
    public static class ReservationResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "예약한 리소스")
        private String resourceName;

        @Schema(description = "예약 상태", example = "CONFIRMED 및 CANCELED")
        private ReservationStatus status;

        @Schema(description = "예약 시작 일시")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시")
        private LocalDateTime endDate;

        public static ReservationResponseListInfo from(Reservations entity) {
            return ReservationResponseListInfo.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .resourceName(entity.getResources().getName())
                    .status(entity.getStatus())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [좌석형] 단일 응답 정보")
    public static class SeatResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "좌석 행")
        private Integer row;

        @Schema(description = "좌석 열")
        private Integer col;

        @Schema(description = "예약 상태", example = "CONFIRMED 및 CANCELED")
        private ReservationStatus status;

        @Schema(description = "예약 시작 일시")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시")
        private LocalDateTime endDate;


        public static SeatResponseListInfo from(Reservations entity) {
            return SeatResponseListInfo.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .row(entity.getRow())
                    .col(entity.getCol())
                    .status(entity.getStatus())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "관리자 예약 목록 조회 [신청형] 단일 응답 정보")
    public static class EventResponseListInfo {

        @Schema(description = "예약 번호")
        private Long id;

        @Schema(description = "예약자")
        private String userName;

        @Schema(description = "이메일")
        private String email;

        @Schema(description = "신청일")
        private LocalDateTime applicationDate;

        @Schema(description = "신청 상태")
        private ReservationStatus status;

        public static EventResponseListInfo from(Reservations entity) {
            return EventResponseListInfo.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .email(entity.getUsers().getEmail())
                    .applicationDate(entity.getCreatedAt())
                    .status(entity.getStatus())
                    .build();
        }
    }


    // =============== 일반 사용자용 ===============
    @Getter
    @Builder
    @Schema(description = "일반 사용자 예약 목록 조회 응답 정보")
    public static class UserResponseList {
        List<UserResponse> reservations;

        public static UserResponseList from(List<Reservations> entities) {
            return UserResponseList.builder()
                    .reservations(entities.stream().map(UserResponse::from).toList())
                    .build();
        }
    }

    @Getter
    @SuperBuilder
    @Schema(description = "일반 사용자 예약 목록 조회 단일 응답 정보")
    public static class UserResponse extends ReservationDetailDto.Response {
        @Schema(description = "예약 시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "예약 종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        public static UserResponse from(Reservations entity) {
            return UserResponse.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .thumbnail(entity.getResources().getResourceGroup().getThumbnail())
                    .resourceGroupName(entity.getResources().getResourceGroup().getName())
                    .resourceName(entity.getResources().getName())
                    .serviceCategory(entity.getResources().getResourceGroup().getCategory())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    // 아래부터는 일반 사용자 예약 목록 조회용 정보
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .build();
        }
    }
}
