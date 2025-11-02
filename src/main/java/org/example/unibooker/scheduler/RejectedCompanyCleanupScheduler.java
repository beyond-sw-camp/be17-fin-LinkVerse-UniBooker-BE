package org.example.unibooker.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.domain.company.service.CompanyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 거절된 기업 자동 정리 스케줄러
 * - REJECTED 상태 기업 자동 삭제 (기본 7일)
 * - 연결된 사용자 계정도 함께 하드 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyCleanupScheduler {

    private final CompanyService companyService;

    @Value("${app.company-cleanup.enabled:true}")
    private boolean enabled;

    @Value("${app.company-cleanup.retention-days:7}")
    private int retentionDays;

    /**
     * 거절된 기업 자동 정리 실행
     * - 매일 새벽 2시 실행 (기본값)
     * - cron 표현식: application.yml에서 설정 가능
     */
    @Scheduled(cron = "${app.company-cleanup.cron:0 0 2 * * *}")
    public void cleanupRejectedCompanies() {
        if (!enabled) {
            log.debug("[RejectedCompanyCleanup] Cleanup is disabled");
            return;
        }

        log.info("[RejectedCompanyCleanup] Batch job started at: {}", LocalDateTime.now());

        try {
            companyService.cleanupRejectedCompanies(retentionDays);
            log.info("[RejectedCompanyCleanup] Batch job completed successfully");
        } catch (Exception e) {
            log.error("[RejectedCompanyCleanup] Batch job failed: {}", e.getMessage(), e);
        }
    }
}