package org.example.apireservation.mapper;

import org.example.apireservation.domain.model.dto.*;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.domain.model.*;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.model.Gender;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationMapper {

    /** Domain -> Entity */
    // 예약하기
    public static Reservations toEntity(Reservation domain) {
        return Reservations.builder()
                .userId(domain.getUserId())
                .resourceId(domain.getResourceId())
                .resourceGroupId(domain.getResourceGroupId())
                .companyId(domain.getCompanyId())
                .createdBy(domain.getUserId())
                .status(ReservationStatus.CONFIRMED)
                .attendeeCount(domain.getHeadCount())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .row(domain.getRow())
                .col(domain.getCol())
                .build();
    }


    /** Entity -> Domain */
    // 예약하기
    public static Reservation from(Reservations entity, Reservation domain) {
        return Reservation.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userName(domain.getUserName())
                .email(domain.getEmail())
                .resourceId(entity.getResourceId())
                .resourceName(domain.getResourceName())
                .resourceImage(domain.getResourceImage())
                .resourceGroupId(entity.getResourceGroupId())
                .resourceGroupName(domain.getResourceGroupName())
                .serviceCategory(domain.getServiceCategory())
                .status(entity.getStatus())
                .headCount(entity.getAttendeeCount())
                .row(entity.getRow())
                .col(entity.getCol())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }


    /** Domain -> Dto */
    // ========================= 예약 상세 조회 =========================
    public static ReservationDetailDto.Response toRes(Reservation domain, List<CustomFieldValueDto> userCustomFieldValues) {
        return switch (domain.getServiceCategory()) {
            case RESERVATION -> toReservationDetailDto(domain, userCustomFieldValues);
            case SEAT -> toSeatDetailDto(domain, userCustomFieldValues);
            case EVENT -> toEventDetailDto(domain, userCustomFieldValues);
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };
    }

    // 예약형 상세 조회
    private static ReservationDetailDto.ReservationResponse toReservationDetailDto(Reservation domain, List<CustomFieldValueDto> customFieldValues) {
        return ReservationDetailDto.ReservationResponse.builder()
                .id(domain.getId())
                .userName(domain.getUserName())
                .status(domain.getStatus())
                .thumbnail(domain.getResourceImage())
                .resourceGroupName(domain.getResourceGroupName())
                .resourceName(domain.getResourceName())
                .serviceCategory(domain.getServiceCategory())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .headCount(domain.getHeadCount())
                .customFieldValues(customFieldValues)
                .build();
    }

    // 좌석형 상세 조회
    private static ReservationDetailDto.SeatResponse toSeatDetailDto(Reservation domain, List<CustomFieldValueDto> customFieldValues) {
        return ReservationDetailDto.SeatResponse.builder()
                .id(domain.getId())
                .userName(domain.getUserName())
                .status(domain.getStatus())
                .thumbnail(domain.getResourceImage())
                .resourceGroupName(domain.getResourceGroupName())
                .resourceName(domain.getResourceName())
                .serviceCategory(domain.getServiceCategory())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .customFieldValues(customFieldValues)
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .headCount(domain.getHeadCount())
                .row(domain.getRow())
                .col(domain.getCol())
                .build();
    }

    // 신청형 상세 조회
    private static ReservationDetailDto.EventResponse toEventDetailDto(Reservation domain, List<CustomFieldValueDto> customFieldValues) {
        return ReservationDetailDto.EventResponse.builder()
                .id(domain.getId())
                .userName(domain.getUserName())
                .status(domain.getStatus())
                .thumbnail(domain.getResourceImage())
                .resourceGroupName(domain.getResourceGroupName())
                .resourceName(domain.getResourceName())
                .serviceCategory(domain.getServiceCategory())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .customFieldValues(customFieldValues)
                .build();
    }

    // ========================= 사용자용 예약 목록 조회 =========================
    public static ReservationListDto.UserResponseList toRes(List<Reservation> domains) {
        List<ReservationListDto.UserResponse> reservations =
                domains.stream().map(domain -> ReservationListDto.UserResponse.builder()
                        .id(domain.getId())
                        .userName(domain.getUserName())
                        .status(domain.getStatus())
                        .thumbnail(domain.getResourceImage())
                        .resourceGroupName(domain.getResourceGroupName())
                        .resourceName(domain.getResourceName())
                        .serviceCategory(domain.getServiceCategory())
                        .createdAt(domain.getCreatedAt())
                        .updatedAt(domain.getUpdatedAt())
                        .deletedAt(domain.getDeletedAt())
                        .startDate(domain.getStartDate())
                        .endDate(domain.getEndDate())
                        .build()).collect(Collectors.toList());

        return ReservationListDto.UserResponseList.builder()
                .reservations(reservations)
                .build();
    }


    // ========================= 플랫폼 관리자 및 기업 관리자 용 예약 목록 조회 =========================
    public static ReservationListDto.ResponseList toRes(List<Reservation> reservations, ServiceCategory serviceCategory) {
        return switch (serviceCategory) {
            case RESERVATION -> toReservationListDto(reservations);
            case SEAT -> toSeatListDto(reservations);
            case EVENT -> toEventListDto(reservations);
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };
    }

    // 예약형 목록 조회
    private static ReservationListDto.ResponseList toReservationListDto(List<Reservation> reservations) {
        List<ReservationListDto.ReservationResponseListInfo> list =
                reservations.stream().map(domain -> ReservationListDto.ReservationResponseListInfo.builder()
                        .id(domain.getId())
                        .userName(domain.getUserName())
                        .resourceName(domain.getResourceName())
                        .status(domain.getStatus())
                        .startDate(domain.getStartDate())
                        .endDate(domain.getEndDate())
                        .build()).toList();

        return ReservationListDto.ResponseList.builder()
                .list(List.copyOf(list))
                .build();
    }

    // 좌석형 목록 조회
    private static ReservationListDto.ResponseList toSeatListDto(List<Reservation> reservations) {
        List<ReservationListDto.SeatResponseListInfo> list = reservations.stream()
                .map(domain -> ReservationListDto.SeatResponseListInfo.builder()
                        .id(domain.getId())
                        .userName(domain.getUserName())
                        .row(domain.getRow())
                        .col(domain.getCol())
                        .status(domain.getStatus())
                        .startDate(domain.getStartDate())
                        .endDate(domain.getEndDate())
                        .build()).toList();

        return ReservationListDto.ResponseList.builder()
                .list(List.copyOf(list))
                .build();
    }

    // 신청형 목록 조회
    private static ReservationListDto.ResponseList toEventListDto(List<Reservation> reservations) {
        List<ReservationListDto.EventResponseListInfo> list = reservations.stream()
                .map(domain -> ReservationListDto.EventResponseListInfo.builder()
                        .id(domain.getId())
                        .userName(domain.getUserName())
                        .email(domain.getEmail())
                        .applicationDate(domain.getCreatedAt())
                        .status(domain.getStatus())
                        .build()).toList();

        return ReservationListDto.ResponseList.builder()
                .list(List.copyOf(list))
                .build();
    }


    // ========================= 특정 리소스별 예약 수 조회 =========================
    public static ReservationTrendDto toResGroupCountList(Object[] result) {
        return ReservationTrendDto.builder()
                .date((LocalDate) result[0])
                .groupId((Long) result[1])
                .count((Integer) result[2])
                .build();
    }

    // ========================= 리소스 별 성과 =========================
    public static ServiceGroupDashBoardDto.ServicePerformanceCount toResReservationCountByGroupResource(Object[] result) {
        return ServiceGroupDashBoardDto.ServicePerformanceCount.builder()
                .resourceId((Long) result[0])
                .count(((Long) result[1]).intValue())
                .build();
    }

    // ========================= 이용자 수 =========================
    public static ServiceGroupDashBoardDto.VisitorCount toResVisitorCount(Integer total, Integer count) {
        return ServiceGroupDashBoardDto.VisitorCount.builder()
                .total(total)
                .count(count)
                .build();
    }

    // ========================= 성별 =========================
    public static ServiceGroupDashBoardDto.GenderReservationCount toResGenderCount(Object[] result) {
        Gender gender = result[0] != null ? (Gender) result[0] : Gender.UNDEFINED;

        return ServiceGroupDashBoardDto.GenderReservationCount.builder()
                .gender(gender)
                .count(((Long) result[1]).intValue())
                .build();
    }

    // ========================= 나이대 =========================
    public static ServiceGroupDashBoardDto.AgeReservationCount toResAgeCount(Object[] result) {
        return ServiceGroupDashBoardDto.AgeReservationCount.builder()
                .age(((Long) result[1]).intValue())
                .count(((Long) result[1]).intValue())
                .build();
    }

    // ========================= 시간대별 예약 현황 =========================
    public static ServiceGroupDashBoardDto.TimeSlotReservationCount toResTimeSlotCount(Object[] result) {
        return ServiceGroupDashBoardDto.TimeSlotReservationCount.builder()
                .hour(((Long) result[0]).intValue())
                .count(((Long) result[1]).intValue())
                .build();
    }
}