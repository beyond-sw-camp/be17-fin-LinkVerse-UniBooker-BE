package org.example.apiresource.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceGroupViewCountScheduler {

    @Value("${app.batch.viewcount.enabled:false}")
    private boolean enabled;

    private final JobLauncher jobLauncher;
    private final Job resourceGroupViewCountJob;

    // 매 시간 정각 실행
    @Scheduled(cron = "0 0 * * * *")
    public void runViewCountBackupJob() {
        if (!enabled) {
            try {
                JobParameters params = new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                        .toJobParameters();

                jobLauncher.run(resourceGroupViewCountJob, params);

                log.info("[Batch] ResourceGroup viewCount 백업 성공");
            } catch (Exception e) {
                log.error("[Batch] ResourceGroup viewCount 백업 실패", e);
            }
        }
    }
}