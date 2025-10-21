package org.example.unibooker.domain.reservation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.*;
import java.util.*;

/**
 * Request - 예약 요청
 * Response - 예약 조회 응답
 **/
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

        @Schema(description = "관리자가 커스텀으로 정의한 필드",
                example = "{\"department_name\": \"인사부\", \"request_note\": \"회의실에 빔프로젝터 필요\"}")
        private Map<String, Object> customFields;

        /** dto -> entity 변환 함수 */
        public Reservations toEntity(Long userId, Resources resource) {
            Users user = Users.builder().id(userId).build();

            // TODO : 신청인지 아닌지 체크
            LocalDateTime startDate = resource.getResourceGroup().getIsAlwaysAvailable() ? date.atStartOfDay() : date.atTime(time);
            LocalDateTime endDate = resource.getResourceGroup().getIsAlwaysAvailable() ? date.atStartOfDay() : startDate.plusMinutes(resource.getTimeInterval().getMinutes());

            // 예약 정원 초과 체크
            if(resource.getResourceGroup().getCategory() == ServiceCategory.SEAT) { // 요일 별 설정 수용인원 만큼 수용 가능
                Integer currentCount = reservationRepository.countByResourceIdAndDate(resource.getId(), startDate.toLocalDate());
                if (currentCount >= resource.getCapacity() || headCount+currentCount >= resource.getCapacity()) {
                    throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
                }
            } else if(resource.getResourceGroup().getCategory() == ServiceCategory.RESERVATION) { // 시간대별 한 타임 예약 가능
                Boolean isReserved = reservationRepository.existsByResourceIdAndTimeRange(resource.getId(), startDate, endDate);
                if (isReserved) {
                    throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
                }
            }

            // 중복 예약 체크
            Boolean isDuplicate = reservationRepository.existsByUserIdAndResourceIdAndStartDateBetween(userId, resource.getId(), startDate, endDate);
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
                    .reservations(entities.stream().map(Response::from).toList())
                    .build();
        }
    }

    // ===================
    // 예약 응답 DTO
    // ===================
    @Getter
    @Builder
    @Schema(description = "예약 조회 응답 정보")
    public static class Response {
        @Schema(description = "예약 번호", example = "1")
        private Long id;

        @Schema(description = "예약자", example = "유현경")
        private String username;

        @Schema(description = "예약 상태", example = "CONFIRMED")
        private ReservationStatus status;

        @Schema(description = "시작 일시", example = "2025-10-16T10:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endDate;

        @Schema(description = "예약한 서비스 항목의 서비스", example = "회의실")
        private String resourceGroup;

        @Schema(description = "예약한 서비스 항목", example = "회의실A")
        private String resource;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        /** entity -> dto 변환 함수 */
        public static Response from(Reservations entity) {
            return Response.builder()
                    .id(entity.getId())
                    .username(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .resourceGroup(entity.getResources().getResourceGroup().getName())
                    .resource(entity.getResources().getName())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
