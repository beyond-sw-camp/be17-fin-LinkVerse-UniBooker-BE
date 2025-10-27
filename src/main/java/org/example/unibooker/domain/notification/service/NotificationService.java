package org.example.unibooker.domain.notification.service;

public class NotificationService {
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
// public void sendInactiveAccountDeletionNotice(List<Users> deletedAccounts) {
//     // 구현 필요
// }
}
