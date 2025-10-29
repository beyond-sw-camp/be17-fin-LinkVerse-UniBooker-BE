package org.example.unibooker.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.notification.model.NotificationStatus;
import org.example.unibooker.domain.notification.model.NotificationType;
import org.example.unibooker.domain.notification.model.entity.Notifications;
import org.example.unibooker.domain.notification.repository.NotificationRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
//import org.example.unibooker.domain.notification.controller.NotificationHandler;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;


/**
 * 알림 서비스
 * - 미활성 계정 삭제 알림 (추후 구현)
 */


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    // -------------------- 알림 대상 타입 조회 --------------------
    @Transactional
    public void sendNotificationToRole(NotificationType type, UserRole role, Object... args) {
        // 1️⃣ 해당 역할 유저 전부 조회
        List<Users> users = userRepository.findByRoleAndStatus(role, UserStatus.ACTIVE, Pageable.unpaged()).getContent();

        // 2️⃣ 각 유저에게 알림 생성 및 전송
        for (Users user : users) {
            sendNotificationToUser(type, user, args);
        }
    }


    // -------------------- 특정 대상에게 알림 전송 --------------------
    @Transactional
    public void sendNotificationToUser(NotificationType type, Users targetUser, Object... args) {
        String title;
        String message;

        try {
            // 1️⃣ args 개수에 따라 %s 자동 포맷 적용
            if (args != null && args.length > 0) {
                title = String.format(type.getTitle(), args);
                message = String.format(type.getMessage(), args);
            } else {
                title = type.getTitle();
                message = type.getMessage();
            }
        } catch (MissingFormatArgumentException e) {
            // %s 개수 안 맞을 때 fallback
            title = type.getTitle();
            message = type.getMessage();
        }

        // 2️⃣ DTO 생성
            NotificationDto.NotificationReq req = NotificationDto.NotificationReq.builder()
                .category(type)
                .title(title)
                .message(message)
                .build();

        // 3️⃣ 엔티티 변환 및 저장
        Notifications notification = req.toEntity(targetUser);
        notificationRepository.save(notification);

        try {
            // 4️⃣ WebSocket 실시간 전송
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(targetUser.getId()),
                    "/queue/notifications",
                    Map.of(
                            "title", title,
                            "message", message,
                            "category", req.getCategory().name()
                    )
            );

            // 5️⃣ 상태 업데이트
            notification.setStatus(NotificationStatus.SENT);
            notificationRepository.save(notification);

            log.info("✅ 알림 전송 성공 - userId: {}, type: {}, title: {}, message: {}", targetUser.getId(), type, title, message);

        } catch (Exception e) {
            // 6️⃣ 실패 처리
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedReason(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);

            log.error("❌ 알림 전송 실패 - userId: {}, type: {}, reason: {}", targetUser.getId(), type, e.getMessage());
        }
    }



    // -------------------- 알림 목록 조회 --------------------
    public Page<NotificationDto.NotificationRes> getUserNotifications(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Notifications> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return NotificationDto.NotificationRes.fromEntityList(notifications);
    }


    // -------------------- 알림 읽음 처리 --------------------
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notifications notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림이 존재하지 않습니다. id=" + notificationId));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * 리소스의 시간 변경 시 해당 리소스를 예약한 고객들에게 알림 전송
     */
//    public void notifyResourceUpdated(Long resourceId, String type) {
//        List<Long> userIds = reservationRepository.findUserIdsByResourceId(resourceId);
//
//        String message = switch (type) {
//            case "regular" -> "리소스의 정규 시간이 변경되었습니다.";
//            case "exception" -> "리소스의 예외 시간이 변경되었습니다.";
//            default -> "리소스 정보가 변경되었습니다.";
//        };
//
//        for (Long userId : userIds) {
//            try {
//                notificationHandler.sendToUser(userId, message);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}

    /**
     * TODO: 미활성 계정 자동 삭제 알림 발송 (팀원 구현)
     *
     * 요구사항:
     * - SUPER 관리자들에게 이메일 알림 발송
     * - 삭제된 계정 목록 포함 (이메일, 역할, 기업명, 생성일)
     * - 총 삭제 개수 및 삭제 일시 포함
     *
     * @param deletedAccounts 삭제된 계정 목록 (List<Users>)
     *
     * 참고 코드:
     * 1. SUPER 관리자 조회:
     *    userRepository.findByRoleAndStatus(UserRole.SUPER, UserStatus.ACTIVE)
     *
     * 2. 이메일 제목: "[UniBooker] 미활성 계정 자동 삭제 알림"
     *
     * 3. 이메일 내용 예시:
     *    - 72시간 이상 첫 로그인 미완료 계정 자동 삭제
     *    - 삭제된 계정 목록 (이메일, 역할, 기업명, 생성일)
     *    - 총 삭제 개수
     *    - 삭제 일시
     */
//    public void sendInactiveAccountDeletionNotice(List<Users> deletedAccounts) {
//        // 추후 구현
//    }
