package org.example.unibooker.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * 거절된 기업 자동 정리 스케줄러
 * - 2주마다 실행 (매주 월요일 중 짝수주만)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedCompanyCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job rejectedCompanyCleanupJob;

    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    /**
     * 거절된 기업 정리 배치 실행
     * - 매주 월요일 새벽 2시에 확인
     * - 짝수주(2,4,6...)에만 실행
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void executeCleanupJob() {
        if (!enabled) {
            log.debug("[RejectedCompanyScheduler] Batch is disabled");
            return;
        }

        // 현재 주차가 짝수주인지 확인
        int weekOfYear = LocalDateTime.now()
                .get(WeekFields.of(Locale.getDefault()).weekOfYear());

        if (weekOfYear % 2 != 0) {
            log.debug("[RejectedCompanyScheduler] Skipped (odd week: {})", weekOfYear);
            return;
        }

        try {
            log.info("[RejectedCompanyScheduler] Starting batch job at: {}", LocalDateTime.now());

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(rejectedCompanyCleanupJob, jobParameters);

            log.info("[RejectedCompanyScheduler] Batch job completed at: {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[RejectedCompanyScheduler] Batch job failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public void executeManually() {
        log.info("[RejectedCompanyScheduler] Manual execution triggered");
        executeCleanupJob();
    }
}