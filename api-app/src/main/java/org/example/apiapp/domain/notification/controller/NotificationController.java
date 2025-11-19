package org.example.apiapp.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.apiapp.domain.notification.model.entity.Notifications;
import org.example.apiapp.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 관리 컨트롤러
 * - 내 알림 목록 조회
 * - 알림 읽음 처리
 * - 읽지 않은 알림 개수 조회
 */
@Slf4j
@Tag(name = "Notification API", description = "알림 관리 API")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /** 알림 서비스 */
    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회
     */
    @Operation(
            summary = "내 알림 목록 조회",
            description = "현재 로그인한 사용자의 알림 목록을 페이징 방식으로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/my")
    public ResponseEntity<Page<Notifications>> getMyNotifications(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications/my - userId: {}", userId);
        Page<Notifications> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 읽음 처리
     */
    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
            }
    )
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("PUT /api/notifications/{}/read - userId: {}", id, userId);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("GET /api/notifications/unread-count - userId: {}", userId);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}