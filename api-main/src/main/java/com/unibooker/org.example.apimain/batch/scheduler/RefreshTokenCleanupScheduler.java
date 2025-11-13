package com.unibooker.main.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Refresh Token 정리 스케줄러
 * - 매일 새벽 2시 실행
 * - 만료된 Refresh Token 자동 삭제
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    /**
     * 배치 활성화 여부 (application.yml에서 설정)
     */
    @Value("${app.batch.token-cleanup.enabled:true}")
    private boolean enabled;

    /**
     * 토큰 보관 기간 (일)
     */
    @Value("${app.batch.token-cleanup.retention-days:7}")
    private int retentionDays;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 만료된 Refresh Token 정리
     * - Cron: 매일 새벽 2시 (0 0 2 * * ?)
     */
    @Scheduled(cron = "${app.batch.token-cleanup.cron:0 0 2 * * ?}")
    public void cleanupExpiredTokens() {
        if (!enabled) {
            log.debug("[RefreshTokenCleanup] Batch is disabled");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        log.info("========================================");
        log.info("[RefreshTokenCleanup] Batch started at: {}",
                startTime.format(FORMATTER));
        log.info("========================================");

        try {
            // TODO: Redis 기반 Refresh Token 저장소 구현 후 활성화
            // tokenStorageService.deleteExpiredTokens(retentionDays);

            // 현재는 로그만 기록
            log.info("[RefreshTokenCleanup] Token retention period: {} days", retentionDays);
            log.info("[RefreshTokenCleanup] TODO: Redis 연동 후 실제 토큰 삭제 구현 예정");

            // 향후 구현 예정 로직:
            // 1. Redis에서 모든 Refresh Token 조회
            // 2. 만료된 토큰 필터링
            // 3. 만료된 토큰 삭제
            // 4. 삭제 건수 반환 및 로깅

            LocalDateTime endTime = LocalDateTime.now();
            log.info("========================================");
            log.info("[RefreshTokenCleanup] Batch completed at: {}",
                    endTime.format(FORMATTER));
            log.info("[RefreshTokenCleanup] Duration: {} ms",
                    java.time.Duration.between(startTime, endTime).toMillis());
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("[RefreshTokenCleanup] Batch failed: {}", e.getMessage(), e);
            log.error("========================================");
        }
    }

    /**
     * 테스트용 수동 실행 메서드
     */
    public void executeManually() {
        log.info("[RefreshTokenCleanup] Manual execution triggered");
        cleanupExpiredTokens();
    }
}