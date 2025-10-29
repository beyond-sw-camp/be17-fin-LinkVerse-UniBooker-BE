package org.example.unibooker.domain.notification.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.unibooker.domain.notification.model.NotificationType;
import org.example.unibooker.domain.notification.model.NotificationStatus;
import org.example.unibooker.domain.notification.model.entity.Notifications;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.domain.Page;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDto {

    @Getter
    @Builder
    @Schema(description = "알림 생성 요청 DTO")
    public static class NotificationReq {

        @Schema(description = "알림 카테고리 (예: NEW_COMPANY_REQUEST)", example = "NEW_COMPANY_REQUEST")
        private NotificationType category;

        @Schema(description = "알림 제목", example = "신규 기업 신청")
        private String title;

        @Schema(description = "알림 메시지", example = "새로운 기업 가입 신청이 접수되었습니다.")
        private String message;

        /**
         * DTO → 엔티티 변환 메서드
         */
        public Notifications toEntity(Users user) {
            return Notifications.builder()
                    .user(user)
                    .category(category)
                    .title(title)
                    .message(message)
                    .status(NotificationStatus.PENDING)
                    .isRead(false)
                    .retryCount(0)
                    .build();
        }
    }


    @Getter
    @Builder
    @Schema(description = "알림 조회")
    public static class NotificationRes {

        @Schema(description = "알림 아이디", example = "1")
        private Long id;

        @Schema(description = "제목", example = "신규 가입 신청")
        private String title;

        @Schema(description = "내용", example = "신규 가입 신청이 접수되었습니다.")
        private String message;

        @Schema(description = "알림 읽음 여부", example = "true")
        private Boolean isRead;

        @Schema(description = "알림 생성 시간", example = "")
        private String createdAt;


        // 단일 엔티티 → DTO 변환
        public static NotificationRes fromEntity(Notifications notification) {
            return NotificationRes.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .isRead(notification.getIsRead())
                    .createdAt(formatTimeAgo(notification.getCreatedAt()))
                    .build();
        }

        // 엔티티 리스트 → DTO 리스트 변환
        public static Page<NotificationRes> fromEntityList(Page<Notifications> notifications) {
            return notifications.map(NotificationRes::fromEntity);
        }

        // 시간 포맷팅
        private static String formatTimeAgo(LocalDateTime createdAt) {
            Duration duration = Duration.between(createdAt, LocalDateTime.now());

            if (duration.toSeconds() < 60) {
                return duration.getSeconds() + "초 전";
            } else if (duration.toMinutes() < 60) {
                return duration.toMinutes() + "분 전";
            } else if (duration.toHours() < 24) {
                return duration.toHours() + "시간 전";
            } else {
                return duration.toDays() + "일 전";
            }
        }
    }
}