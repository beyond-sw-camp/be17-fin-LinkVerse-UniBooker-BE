package org.example.apiapp.batch.scheduler;

import org.example.apiapp.domain.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 거절된 기업 자동 정리 스케줄러
 * - 2주마다 실행 (매주 월요일 중 짝수주만)
 * - 거절된 기업 및 연결된 계정 하드 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyCleanupScheduler {

    private final CompanyService companyService;

    /**
     * 배치 활성화 여부
     */
    @Value("${app.batch.rejected-company.enabled:true}")
    private boolean enabled;

    /**
     * 거절된 기업 보관 기간 (일) - 기본 3일
     */
    @Value("${app.batch.rejected-company.retention-days:3}")
    private int retentionDays;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 거절된 기업 정리 배치 실행
     * - Cron: 매주 월요일 새벽 2시 (0 0 2 ? * MON)
     * - 짝수주에만 실행
     */
    @Scheduled(cron = "${app.batch.rejected-company.cron:0 0 2 ? * MON}")
    public void cleanupRejectedCompanies() {
        if (!enabled) {
            log.debug("[RejectedCompanyCleanup] Batch is disabled");
            return;
        }

        // 현재 주차가 짝수주인지 확인
        int weekOfYear = LocalDateTime.now()
                .get(WeekFields.of(Locale.getDefault()).weekOfYear());

        if (weekOfYear % 2 != 0) {
            log.debug("[RejectedCompanyCleanup] Skipped (odd week: {})", weekOfYear);
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("========================================");
        log.info("[RejectedCompanyCleanup] Batch started at: {}",
                startTime.format(FORMATTER));
        log.info("[RejectedCompanyCleanup] Week of year: {} (even week)", weekOfYear);
        log.info("========================================");

        try {
            log.info("[RejectedCompanyCleanup] Retention period: {} days", retentionDays);

            // CompanyService의 기존 메서드 호출
            companyService.cleanupRejectedCompanies(retentionDays);

            LocalDateTime endTime = LocalDateTime.now();
            log.info("========================================");
            log.info("[RejectedCompanyCleanup] Batch completed at: {}",
                    endTime.format(FORMATTER));
            log.info("[RejectedCompanyCleanup] Duration: {} ms",
                    java.time.Duration.between(startTime, endTime).toMillis());
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("[RejectedCompanyCleanup] Batch failed: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public void executeManually() {
        log.info("[RejectedCompanyCleanup] Manual execution triggered");
        cleanupRejectedCompanies();
    }
}