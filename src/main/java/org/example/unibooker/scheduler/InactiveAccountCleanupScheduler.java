package org.example.unibooker.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.infrastructure.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 미활성 계정 자동 삭제 스케줄러
 * - ADMIN/MANAGER 중 첫 로그인 미완료 계정 자동 정리 (72시간)
 * - 하드 삭제 (DB에서 완전 제거)
 * - 삭제 시 본인에게 이메일 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountCleanupScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.account-cleanup.inactive-minutes:4320}")  // 기본값: 4320분 = 72시간
    private int inactiveMinutes;

    @Value("${app.account-cleanup.enabled:true}")
    private boolean enabled;

    /**
     * 미활성 계정 자동 삭제 실행
     * - 매일 새벽 2시 실행 (기본값)
     * - cron 표현식: application.yml에서 설정 가능
     */
    @Scheduled(cron = "${app.account-cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void cleanupInactiveAccounts() {
        if (!enabled) {
            log.debug("[InactiveAccountCleanup] Cleanup is disabled");
            return;
        }

        log.info("[InactiveAccountCleanup] Start: {}", LocalDateTime.now());
        log.info("[InactiveAccountCleanup] Inactive threshold: {} minutes", inactiveMinutes);

        try {
            // 1. 삭제 대상 조회
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(inactiveMinutes);
            log.info("[InactiveAccountCleanup] Cutoff time: {}", cutoffTime);

            List<Users> inactiveAccounts = userRepository
                    .findByRoleInAndStatusAndIsFirstLoginAndCreatedAtBefore(
                            List.of(UserRole.ADMIN, UserRole.MANAGER),
                            UserStatus.ACTIVE,
                            true,
                            cutoffTime
                    );

            if (inactiveAccounts.isEmpty()) {
                log.info("[InactiveAccountCleanup] No inactive accounts found");
                return;
            }

            log.info("[InactiveAccountCleanup] Found {} inactive accounts",
                    inactiveAccounts.size());

            // 2. 삭제 전 로그 기록 및 본인에게 이메일 발송
            for (Users account : inactiveAccounts) {
                log.info("- {} (Role: {}, Company ID: {}, Created: {})",
                        account.getEmail(),
                        account.getRole(),
                        account.getCompany().getId(),
                        account.getCreatedAt());

                // 감사 추적용 상세 로그
                log.warn("[HARD_DELETE] id={}, email={}, role={}, companyId={}, name={}, createdAt={}",
                        account.getId(),
                        account.getEmail(),
                        account.getRole(),
                        account.getCompany().getId(),
                        account.getName(),
                        account.getCreatedAt());

                // 본인에게 삭제 완료 이메일 발송
                try {
                    emailService.sendAccountDeletionNotice(
                            account.getEmail(),
                            account.getName(),
                            account.getRole()
                    );
                    log.info("[InactiveAccountCleanup] Deletion notice sent to: {}",
                            account.getEmail());
                } catch (Exception e) {
                    log.error("[InactiveAccountCleanup] Failed to send email to {}: {}",
                            account.getEmail(), e.getMessage());
                    // 이메일 실패해도 삭제는 진행
                }
            }

            // 3. 하드 삭제 (DB에서 완전 제거)
            userRepository.deleteAll(inactiveAccounts);

            log.info("[InactiveAccountCleanup] Permanently deleted: {} accounts",
                    inactiveAccounts.size());
            log.info("[InactiveAccountCleanup] End: {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[InactiveAccountCleanup] Error occurred: {}", e.getMessage(), e);
        }
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public void executeManually() {
        log.info("[InactiveAccountCleanup] Manual execution triggered");
        cleanupInactiveAccounts();
    }
}