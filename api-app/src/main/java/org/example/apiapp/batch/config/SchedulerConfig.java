package org.example.apiapp.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄러 설정
 * - Spring @Scheduled 활성화
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // @EnableScheduling으로 스케줄러 기능 활성화
}