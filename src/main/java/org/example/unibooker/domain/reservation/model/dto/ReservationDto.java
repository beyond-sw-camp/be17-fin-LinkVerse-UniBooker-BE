package org.example.unibooker.domain.reservation.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.user.model.entity.Users;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Request - 예약 요청
 * Response - 예약 조회 응답
 **/
public class ReservationDto {

    // ===================
    // 예약 요청 DTO
    // ===================
    @Getter
    @Schema(description = "예약시 필요 요청 정보")
    public static class Request {
        @Schema(description = "예약할 날짜", example = "2025-10-16")
        private LocalDate date;

        @Schema(description = "예약할 시간", example = "10:00")
        private LocalTime time;

        @Schema(description = "인원수", example = "3")
        private Integer headCount;

        @Schema(description = "관리자가 커스텀으로 정의한 필드",
                example = "{\"department_name\": \"인사부\", \"request_note\": \"회의실에 빔프로젝터 필요\"}")
        private Map<String, Object> customFields;

        /** dto -> entity 변환 */
        public Reservations toEntity(Long userId, Resources resource) {
            Users user = Users.builder().id(userId).build();
            LocalDateTime startTime = date.atTime(time);
            LocalDateTime endTime = startTime.plusMinutes(resource.getTimeInterval().getMinutes());

            return Reservations.builder()
                    .users(user)
                    .resources(resource)
                    .createdBy(user)
                    .status(ReservationStatus.CONFIRMED)
                    .attendeeCount(headCount)
                    .startTime(startTime)
                    .endTime(endTime)
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
        private LocalDateTime startTime;

        @Schema(description = "종료 일시", example = "2025-10-16T11:00:00")
        private LocalDateTime endTime;

        @Schema(description = "예약한 서비스 항목의 서비스", example = "회의실")
        private String resourceGroup;

        @Schema(description = "예약한 서비스 항목", example = "회의실A")
        private String resource;

        @Schema(description = "생성일시")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;

        /** entity -> dto 변환 */
        public static Response from(Reservations entity) {
            return Response.builder()
                    .id(entity.getId())
                    .username(entity.getUsers().getName())
                    .status(entity.getStatus())
                    .startTime(entity.getStartTime())
                    .endTime(entity.getEndTime())
                    .resourceGroup(entity.getResources().getResourceGroup().getName())
                    .resource(entity.getResources().getName())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
