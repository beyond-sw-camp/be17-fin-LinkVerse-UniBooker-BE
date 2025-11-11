package org.example.apireservation.mapper;

import org.example.apireservation.adapter.out.Reservations;
import org.example.apireservation.domain.model.Reservation;
import org.example.apireservation.domain.model.ReservationStatus;
import org.example.apireservation.usecase.port.out.CustomFieldValueDto;
import org.example.apireservation.usecase.port.out.ReservationDetailDto;

import java.util.List;

public class ReservationMapper {

    // Domain -> Entity
    public static Reservations toEntity(Reservation domain) {
        return Reservations.builder()
                .userId(domain.getUserId())
                .resourceId(domain.getResourceId())
                .createdBy(domain.getUserId())
                .status(ReservationStatus.CONFIRMED)
                .attendeeCount(domain.getHeadCount())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .row(domain.getRow())
                .col(domain.getCol())
                .build();
    }

    // Entity -> Domain
    public static Reservation fromEntity(Reservations entity) {
        return Reservation.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .resourceId(entity.getResourceId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .headCount(entity.getAttendeeCount())
                .row(entity.getRow())
                .col(entity.getCol())
                .build();
    }

    // Command -> Domain
    // Domain -> Command

    public static ReservationDetailDto.Response toDetailDto(Reservations entity, List<CustomFieldValueDto> customFields) {
        return switch (entity.getResources().getResourceGroup().getCategory()) {
            case RESERVATION -> ReservationDetailDto.ReservationResponse.from(entity, customFields);
            case SEAT -> ReservationDetailDto.SeatResponse.from(entity, customFields);
            case EVENT -> ReservationDetailDto.EventResponse.from(entity, customFields);
        };
    }

    public static ReservationListDto.ResponseList toListDto(List<Reservations> entities, ServiceCategory category) {
        return ReservationListDto.ResponseList.from(entities, category);
    }
}
