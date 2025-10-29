package org.example.unibooker.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ResourceItemScheduler {

    private final JobLauncher jobLauncher;
    private final Job resourceStatusJob;

    // 매일 00:00:00 실행
    @Scheduled(cron = "0 0 0 * * *")
    public void runResourceStatusJob() {
        try {
            log.info("🦞 [배치 시작] 리소스 상태 업데이트 배치 실행 중...");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                    .toJobParameters();

            jobLauncher.run(resourceStatusJob, jobParameters);

            log.info("✅ [배치 완료] 리소스 상태 업데이트 완료");
        } catch (Exception e) {
            log.error("❌ [배치 실패] 리소스 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
