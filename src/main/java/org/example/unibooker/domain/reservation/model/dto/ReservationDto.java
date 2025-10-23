package org.example.unibooker.domain.reservation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.*;
import java.util.*;

public class ReservationDto {

    // ===================
    // 예약 요청 DTO
    // ===================
    @Getter
    @RequiredArgsConstructor
    @Schema(description = "예약시 필요 요청 정보")
    public static class Request {
        private final ReservationRepository reservationRepository;

        @Schema(description = "예약할 날짜", example = "2025-10-16")
        private LocalDate date;

        @Schema(description = "예약할 시간", example = "10:00")
        private LocalTime time;

        @Schema(description = "인원수", example = "3")
        private Integer headCount;

        @Schema(description = "좌석 행", example = "1")
        private Integer row;

        @Schema(description = "좌석 열", example = "2")
        private Integer col;

        @Schema(description = "리소스에 등록되어 있는 사용자 입력 커스텀 필드 값")
        private List<CustomFieldDto.CustomFieldValue> customFieldValues;

        /** dto -> entity 변환 함수 */
        public Reservations toReservationEntity(Long userId, Resources resource) {
            Users user = Users.builder().id(userId).build();
            LocalDateTime startDate = null, endDate = null;

            // 신청인지 아닌지 체크 - 신청이면 날짜/시간 저장 안함(null). 신청일은 createdAt 으로 구별
            if(!resource.getResourceGroup().getCategory().equals(ServiceCategory.EVENT)) {
                startDate = date.atTime(time);
                endDate = startDate.plusMinutes(resource.getTimeInterval().getMinutes());
            }

            // 정원 초과 체크
            if(resource.getResourceGroup().getCategory().equals(ServiceCategory.SEAT)) { // 요일 별 설정 수용인원 만큼 수용 가능
                Integer currentCount = reservationRepository.countByResourceIdAndDate(resource.getId(), startDate.toLocalDate());
                if ((currentCount >= resource.getCapacity())) {
                    throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
                }
            } else if(resource.getResourceGroup().getCategory().equals(ServiceCategory.RESERVATION)) { // 시간대별 한 타임 예약 가능
                Boolean isReserved = reservationRepository.existsByResourceIdAndTimeRange(resource.getId(), startDate, endDate);
                if (isReserved) {
                    throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
                }
            } else if(resource.getResourceGroup().getCategory().equals(ServiceCategory.EVENT)) { // 수용인원 만큼 수용 가능
                Integer currentCount = reservationRepository.countByResourcesId(resource.getId());
                if((currentCount >= resource.getCapacity())) {
                    throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
                }
            }

            // 중복 예약 체크
            Boolean isDuplicate;
            if(!(resource.getResourceGroup().getCategory() == ServiceCategory.EVENT)) {
                isDuplicate = reservationRepository.existsByUserIdAndResourceIdAndStartDateBetween(userId, resource.getId(), startDate, endDate);
            } else {
                isDuplicate = reservationRepository.existsByUserIdAndResourceIdAndStartDateBetween(userId, resource.getId(), resource.getStartDate().atStartOfDay(), resource.getEndDate().atStartOfDay());
            }
            if (isDuplicate) {
                throw new BaseException(BaseResponseStatus.RESERVATION_DUPLICATED);
            }

            // 예약 Entity 반환
            return Reservations.builder()
                    .users(user)
                    .resources(resource)
                    .createdBy(user)
                    .status(ReservationStatus.CONFIRMED)
                    .attendeeCount(headCount)
                    .startDate(startDate)
                    .endDate(endDate)
                    .row(row)
                    .col(col)
                    .build();
        }
    }


    // ===================
    // 예약 목록 응답 DTO
    // ===================
    @Getter
    @Builder
    @Schema(description = "예약 목록 조회 응답 정보")
    public static class ResponseList {
        @Schema(description = "예약 목록")
        private List<Response> reservations;

        public static ResponseList from(List<Reservations> entities) {
            return ResponseList.builder()
//                    .reservations(entities.stream().map(Response::from).toList())
                    .build();
        }
    }

    public static class ReservationResponseListInfo {

    }

    public static class SeatResponseListInfo {
        private String resourceName;
        private LocalDateTime startDate; // 시작 일시 2025-10-22T10:00:00
        private LocalDateTime endDate; // 종료 일시 2025-10-22T11:00:00
        private Integer headCount; // 예약한 인원 수
        private Integer capacity; // 수용 인원
    }

    public static class EventResponseListInfo {
        private String resourceName;
        private LocalDateTime startDate; // 신청 시작 일시 2025-10-22T00:00:00
        private LocalDateTime endDate; // 신청 종료 일시 2025-11-01T00:00:00
    }


    // ===================
    // 예약 상세 응답 DTO
    // ===================
    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [공통] 응답 정보")
    public abstract static class Response {
        @Schema(description = "예약 번호", example = "1")
        private Long id;

        @Schema(description = "예약자", example = "유현경")
        private String userName;

        @Schema(description = "예약 상태", example = "CONFIRMED")
        private ReservationStatus status;

        @Schema(description = "예약한 서비스 항목의 서비스", example = "회의실")
        private String resourceGroupName;

        @Schema(description = "예약한 서비스 항목", example = "회의실A")
        private String resourceName;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        @Schema(description = "삭제일시")
        private LocalDateTime deletedAt;

        @Schema(description = "리소스에 등록되어 있는 사용자 입력 커스텀 필드")
        private List<CustomFieldDto.CustomFieldValueListRes> customFieldValues;
    }

    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [예약형] 응답 정보")
    public static class ReservationResponse extends Response {
        @Schema(description = "시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        @Schema(description = "인원수")
        private Integer headCount;

        /** entity -> dto 로 변환 */
        public static ReservationResponse from(Reservations entity, List<CustomFieldDto.CustomFieldValueListRes> userCustomFieldValues) {
            return ReservationResponse.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .resourceGroupName(entity.getResources().getResourceGroup().getName())
                    .resourceName(entity.getResources().getName())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    .customFieldValues(userCustomFieldValues)
                    // 아래부터는 예약형 정보
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .headCount(entity.getAttendeeCount())
                    .build();
        }
    }

    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [좌석형] 응답 정보")
    public static class SeatResponse extends Response {
        @Schema(description = "시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        @Schema(description = "인원수")
        private Integer headCount;

        @Schema(description = "좌석 행")
        private Integer seatRow;

        @Schema(description = "좌석 열")
        private Integer seatCol;

        public static SeatResponse from(Reservations entity, List<CustomFieldDto.CustomFieldValueListRes> userCustomFieldValues) {
            return SeatResponse.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .resourceGroupName(entity.getResources().getResourceGroup().getName())
                    .resourceName(entity.getResources().getName())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    .customFieldValues(userCustomFieldValues)
                    // 아래부터는 좌석형 정보
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .headCount(entity.getAttendeeCount())
                    .seatRow(entity.getRow())
                    .seatCol(entity.getCol())
                    .build();
        }
    }

    @Getter
    @SuperBuilder
    @Schema(description = "예약 상세 조회 [신청형] 응답 정보")
    public static class EventResponse extends Response{
        public static EventResponse from(Reservations entity, List<CustomFieldDto.CustomFieldValueListRes> userCustomFieldValues) {
            return EventResponse.builder()
                    .id(entity.getId())
                    .userName(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .resourceGroupName(entity.getResources().getResourceGroup().getName())
                    .resourceName(entity.getResources().getName())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .deletedAt(entity.getDeletedAt())
                    .customFieldValues(userCustomFieldValues)
                    .build();
        }
    }
}
