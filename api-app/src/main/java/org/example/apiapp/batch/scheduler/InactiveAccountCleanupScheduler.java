package org.example.apiapp.batch.scheduler;

import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.user.repository.UserRepository;
import org.example.apiapp.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * 미활성 계정 자동 정리 스케줄러
 * - 2주마다 실행 (매주 월요일 중 짝수주만)
 * - ADMIN/MANAGER 중 첫 로그인 미완료 계정 자동 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountCleanupScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * 배치 활성화 여부
     */
    @Value("${app.batch.inactive-account.enabled:true}")
    private boolean enabled;

    /**
     * 미활성 기준 시간 (분) - 기본 4320분 = 3일
     */
    @Value("${app.batch.inactive-account.inactive-minutes:4320}")
    private int inactiveMinutes;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 미활성 계정 정리 배치 실행
     * - Cron: 매주 월요일 새벽 2시 (0 0 2 ? * MON)
     * - 짝수주에만 실행
     */
    @Scheduled(cron = "${app.batch.inactive-account.cron:0 0 2 ? * MON}")
    @Transactional
    public void cleanupInactiveAccounts() {
        if (!enabled) {
            log.debug("[InactiveAccountCleanup] Batch is disabled");
            return;
        }

        // 현재 주차가 짝수주인지 확인
        int weekOfYear = LocalDateTime.now()
                .get(WeekFields.of(Locale.getDefault()).weekOfYear());

        if (weekOfYear % 2 != 0) {
            log.debug("[InactiveAccountCleanup] Skipped (odd week: {})", weekOfYear);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("========================================");
        log.info("[InactiveAccountCleanup] Batch started at: {}",
                startTime.format(FORMATTER));
        log.info("[InactiveAccountCleanup] Week of year: {} (even week)", weekOfYear);
        log.info("========================================");

        try {
            // 1. 미활성 계정 조회
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(inactiveMinutes);
            log.info("[InactiveAccountCleanup] Cutoff time: {}", cutoffTime.format(FORMATTER));
            log.info("[InactiveAccountCleanup] Inactive period: {} minutes ({} days)",
                    inactiveMinutes, inactiveMinutes / 1440);

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

            // 2. 계정 정보 로깅 및 이메일 발송 (삭제 전)
            for (Users account : inactiveAccounts) {
                log.warn("[BATCH_HARD_DELETE] id={}, email={}, role={}, companyId={}, name={}, createdAt={}",
                        account.getId(),
                        account.getEmail(),
                        account.getRole(),
                        account.getCompanyId(),
                        account.getName(),
                        account.getCreatedAt());

                // 이메일 발송
                try {
                    emailService.sendAccountDeletionNotice(
                            account.getEmail(),
                            account.getName(),
                            account.getRole()
                    );
                    log.info("[InactiveAccountCleanup] Deletion notice sent to: {}", account.getEmail());
                } catch (Exception e) {
                    log.error("[InactiveAccountCleanup] Failed to send email to {}: {}",
                            account.getEmail(), e.getMessage());
                    // 이메일 발송 실패해도 계정 삭제는 진행
                }
            }

            // 3. 계정 하드 삭제
            userRepository.deleteAll(inactiveAccounts);
            log.info("[InactiveAccountCleanup] Successfully deleted {} accounts",
                    inactiveAccounts.size());

            LocalDateTime endTime = LocalDateTime.now();
            log.info("========================================");
            log.info("[InactiveAccountCleanup] Batch completed at: {}",
                    endTime.format(FORMATTER));
            log.info("[InactiveAccountCleanup] Deleted count: {}", inactiveAccounts.size());
            log.info("[InactiveAccountCleanup] Duration: {} ms",
                    java.time.Duration.between(startTime, endTime).toMillis());
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("[InactiveAccountCleanup] Batch failed: {}", e.getMessage(), e);
            log.error("========================================");
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