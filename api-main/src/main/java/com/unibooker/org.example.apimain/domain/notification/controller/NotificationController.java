package com.unibooker.main.domain.notification.controller;

import com.unibooker.main.domain.notification.model.entity.Notifications;
import com.unibooker.main.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 알림 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<Notifications>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications/my - userId: {}", userId);
        Page<Notifications> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("PUT /api/notifications/{}/read - userId: {}", id, userId);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 읽지 않은 알림 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        log.info("GET /api/notifications/unread-count - userId: {}", userId);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}