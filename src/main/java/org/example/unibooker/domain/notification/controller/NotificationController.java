package org.example.unibooker.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 API 컨트롤러
 * - ERD notifications 테이블 기반
 * - 모든 권한(SUPER, ADMIN, USER)이 본인의 알림만 조회/관리 가능
 */
@Tag(name = "Notification API", description = "알림 관리 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    // private final NotificationService notificationService; // TODO: Service 연결

    // ========== 알림 목록 조회 ==========

    /**
     * 알림 목록 조회
     */
    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자의 알림 목록을 페이징하여 조회합니다. " +
                    "읽음/안읽음 필터링 가능하며, 최신순 정렬됩니다."
    )
    @GetMapping
    public BaseResponse<NotificationDto.PageResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isRead,
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.PageResponse response =
        //     notificationService.getNotifications(userId, page, size, isRead);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }

    // ========== 읽지 않은 알림 개수 조회 ==========

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다. 헤더 알림 뱃지 표시에 사용됩니다."
    )
    @GetMapping("/unread-count")
    public BaseResponse<NotificationDto.UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.UnreadCountResponse response =
        //     notificationService.getUnreadCount(userId);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }

    // ========== 알림 단건 조회 ==========

    /**
     * 알림 상세 조회
     */
    @Operation(
            summary = "알림 상세 조회",
            description = "특정 알림의 상세 정보를 조회합니다. 본인의 알림만 조회 가능합니다."
    )
    @GetMapping("/{notificationId}")
    public BaseResponse<NotificationDto.DetailResponse> getNotificationDetail(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.DetailResponse response =
        //     notificationService.getNotificationById(userId, notificationId);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }

    // ========== 알림 읽음 처리 ==========

    /**
     * 알림 읽음 처리
     */
    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다. 본인의 알림만 처리 가능합니다."
    )
    @PutMapping("/{notificationId}/read")
    public BaseResponse<NotificationDto.ReadResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.ReadResponse response =
        //     notificationService.markAsRead(userId, notificationId);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }

    // ========== 전체 알림 읽음 처리 ==========

    /**
     * 전체 알림 읽음 처리
     */
    @Operation(
            summary = "전체 알림 읽음 처리",
            description = "로그인한 사용자의 읽지 않은 모든 알림을 읽음 상태로 변경합니다."
    )
    @PutMapping("/read-all")
    public BaseResponse<NotificationDto.ReadAllResponse> markAllAsRead(
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.ReadAllResponse response =
        //     notificationService.markAllAsRead(userId);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }

    // ========== 알림 삭제 ==========

    /**
     * 알림 삭제 (소프트 삭제)
     */
    @Operation(
            summary = "알림 삭제",
            description = "특정 알림을 삭제합니다. 소프트 삭제 방식으로 deleted_at이 설정됩니다."
    )
    @DeleteMapping("/{notificationId}")
    public BaseResponse<NotificationDto.DeleteResponse> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Long userId) {

        // TODO: Service 구현 후 연결
        // NotificationDto.DeleteResponse response =
        //     notificationService.deleteNotification(userId, notificationId);
        // return BaseResponse.success(response);

        return BaseResponse.success(null);
    }
}