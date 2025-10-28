package org.example.unibooker.domain.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
//import org.example.unibooker.domain.notification.controller.NotificationHandler;
import org.example.unibooker.domain.notification.model.dto.NotificationDto;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;


/**
 * 알림 서비스
 * - 미활성 계정 삭제 알림 (추후 구현)
 */


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
//    private final NotificationHandler notificationHandler;
    private final ReservationRepository reservationRepository;

    public void sendLoginNotification(Long userId) {
        // userId는 STOMP 세션에서 username으로 인식되는 값이어야 함
        String message = "로그인 성공! 알림 테스트입니다.";
        messagingTemplate.convertAndSendToUser(
                userId.toString(),       // username
                "/queue/notifications",  // 프론트 구독 경로
                message
        );
    }

    public void sendResourceUpdate(Long userId, String message) {
        NotificationDto.notificationReq dto = NotificationDto.notificationReq.fromMessage("정규 시간이 변경되었습니다.");

        messagingTemplate.convertAndSendToUser(
                userId.toString(), // username or userId
                "/queue/notifications", // 프론트에서 구독한 경로
                dto // 메시지 객체 전달
        );
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
