package org.example.unibooker.domain.notification.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.notification.model.NotificationCategory;
import org.example.unibooker.domain.notification.model.NotificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDto {

    // ========== 알림 생성 Request (시스템 내부 호출용) ==========

    /**
     * 알림 생성 요청 DTO (시스템 내부 호출용)
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "알림 생성 요청 (시스템 내부 호출용)")
    public static class CreateRequest {

        @NotNull(message = "사용자 ID는 필수입니다")
        @Schema(description = "알림 받을 사용자 ID (user_id)",
                example = "1",
                required = true)
        private Long userId;

        @NotNull(message = "알림 카테고리는 필수입니다")
        @Schema(description = "알림 카테고리 (category)",
                example = "RESERVATION_CONFIRMED",
                required = true)
        private NotificationCategory category;

        @NotBlank(message = "알림 메시지는 필수입니다")
        @Size(max = 500, message = "알림 메시지는 500자를 초과할 수 없습니다")
        @Schema(description = "알림 메시지 (message)",
                example = "회의실 예약이 확정되었습니다.",
                required = true)
        private String message;

        @Builder
        public CreateRequest(Long userId, NotificationCategory category, String message) {
            this.userId = userId;
            this.category = category;
            this.message = message;
        }
    }

    // ========== 알림 목록 응답 ==========

    /**
     * 알림 목록 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "알림 목록 응답")
    public static class ListResponse {

        @Schema(description = "알림 ID (notification_id)", example = "1")
        private Long notificationId;

        @Schema(description = "알림 카테고리 (category)", example = "RESERVATION_CONFIRMED")
        private NotificationCategory category;

        @Schema(description = "알림 카테고리 한글명", example = "예약 확정")
        private String categoryName;

        @Schema(description = "알림 메시지 (message)", example = "회의실 예약이 확정되었습니다.")
        private String message;

        @Schema(description = "발송 상태 (status)", example = "SENT")
        private NotificationStatus status;

        @Schema(description = "발송 상태 한글명", example = "발송 완료")
        private String statusName;

        @Schema(description = "읽음 여부 (is_read)", example = "false")
        private Boolean isRead;

        @Schema(description = "생성 일시 (created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "읽은 일시 (read_at)", example = "2025-10-16T15:00:00")
        private LocalDateTime readAt;
    }

    // ========== 알림 상세 응답 ==========

    /**
     * 알림 상세 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "알림 상세 응답")
    public static class DetailResponse {

        @Schema(description = "알림 ID (notification_id)", example = "1")
        private Long notificationId;

        @Schema(description = "사용자 ID (user_id)", example = "100")
        private Long userId;

        @Schema(description = "알림 카테고리 (category)", example = "RESERVATION_CONFIRMED")
        private NotificationCategory category;

        @Schema(description = "알림 카테고리 한글명", example = "예약 확정")
        private String categoryName;

        @Schema(description = "알림 메시지 (message)", example = "회의실 예약이 확정되었습니다.")
        private String message;

        @Schema(description = "발송 상태 (status)", example = "SENT")
        private NotificationStatus status;

        @Schema(description = "발송 상태 한글명", example = "발송 완료")
        private String statusName;

        @Schema(description = "읽음 여부 (is_read)", example = "false")
        private Boolean isRead;

        @Schema(description = "읽은 일시 (read_at)", example = "2025-10-16T15:00:00")
        private LocalDateTime readAt;

        @Schema(description = "발송 실패 사유 (failed_reason)", example = "이메일 전송 실패")
        private String failedReason;

        @Schema(description = "생성 일시 (created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시 (updated_at)", example = "2025-10-16T15:00:00")
        private LocalDateTime updatedAt;

        @Schema(description = "삭제 일시 (deleted_at)", example = "2025-10-16T16:00:00")
        private LocalDateTime deletedAt;
    }

    // ========== 읽지 않은 알림 개수 응답 ==========

    /**
     * 읽지 않은 알림 개수 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "읽지 않은 알림 개수 응답")
    public static class UnreadCountResponse {

        @Schema(description = "읽지 않은 알림 개수", example = "5")
        private Integer unreadCount;
    }

    // ========== 페이징 응답 ==========

    /**
     * 알림 목록 페이징 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "알림 목록 페이징 응답")
    public static class PageResponse {

        @Schema(description = "알림 목록")
        private List<ListResponse> notifications;

        @Schema(description = "전체 요소 개수", example = "50")
        private Long totalElements;

        @Schema(description = "전체 페이지 수", example = "3")
        private Integer totalPages;

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지 크기", example = "20")
        private Integer pageSize;

        @Schema(description = "첫 페이지 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "마지막 페이지 여부", example = "false")
        private Boolean isLast;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        private Boolean hasPrevious;
    }

    // ========== 알림 읽음 처리 응답 ==========

    /**
     * 알림 읽음 처리 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "알림 읽음 처리 응답")
    public static class ReadResponse {

        @Schema(description = "처리된 알림 ID (notification_id)", example = "1")
        private Long notificationId;

        @Schema(description = "읽음 여부 (is_read)", example = "true")
        private Boolean isRead;

        @Schema(description = "읽음 처리 일시 (read_at)", example = "2025-10-16T15:00:00")
        private LocalDateTime readAt;

        @Schema(description = "처리 결과 메시지", example = "알림이 읽음 처리되었습니다.")
        private String message;
    }

    // ========== 전체 읽음 처리 응답 ==========

    /**
     * 전체 알림 읽음 처리 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "전체 알림 읽음 처리 응답")
    public static class ReadAllResponse {

        @Schema(description = "처리된 알림 개수", example = "5")
        private Integer updatedCount;

        @Schema(description = "처리 결과 메시지", example = "5개의 알림이 읽음 처리되었습니다.")
        private String message;

        @Schema(description = "처리 일시", example = "2025-10-16T15:00:00")
        private LocalDateTime processedAt;
    }

    // ========== 알림 삭제 응답 ==========

    /**
     * 알림 삭제 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "알림 삭제 응답")
    public static class DeleteResponse {

        @Schema(description = "삭제된 알림 ID (notification_id)", example = "1")
        private Long notificationId;

        @Schema(description = "삭제 일시 (deleted_at)", example = "2025-10-16T16:00:00")
        private LocalDateTime deletedAt;

        @Schema(description = "처리 결과 메시지", example = "알림이 삭제되었습니다.")
        private String message;
    }
}