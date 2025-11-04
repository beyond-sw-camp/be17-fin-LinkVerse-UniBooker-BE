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
 * 미활성 계정 자동 삭제 스케줄러
 * - 2주마다 실행 (매주 월요일 중 짝수주만)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InactiveAccountCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job inactiveAccountCleanupJob;

    @Value("${app.batch.enabled:true}")
    private boolean enabled;

    /**
     * 미활성 계정 정리 배치 실행
     * - 매주 월요일 새벽 2시에 확인
     * - 짝수주(2,4,6...)에만 실행
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void executeCleanupJob() {
        if (!enabled) {
            log.debug("[InactiveAccountScheduler] Batch is disabled");
            return;
        }

        // 현재 주차가 짝수주인지 확인
        int weekOfYear = LocalDateTime.now()
                .get(WeekFields.of(Locale.getDefault()).weekOfYear());

        if (weekOfYear % 2 != 0) {
            log.debug("[InactiveAccountScheduler] Skipped (odd week: {})", weekOfYear);
            return;
        }

        try {
            log.info("[InactiveAccountScheduler] Starting batch job at: {}", LocalDateTime.now());

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(inactiveAccountCleanupJob, jobParameters);

            log.info("[InactiveAccountScheduler] Batch job completed at: {}", LocalDateTime.now());

        } catch (Exception e) {
            log.error("[InactiveAccountScheduler] Batch job failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public void executeManually() {
        log.info("[InactiveAccountScheduler] Manual execution triggered");
        executeCleanupJob();
    }
}