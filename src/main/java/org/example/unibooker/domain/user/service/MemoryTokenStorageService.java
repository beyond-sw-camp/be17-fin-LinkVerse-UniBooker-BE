package org.example.unibooker.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 메모리 기반 Refresh Token 저장소 (임시 구현체)
 * - ConcurrentHashMap 사용
 * - TTL 자동 만료 처리
 * - Thread-safe 보장
 *
 * TODO: Redis 연동 시 RedisTokenStorageService로 교체 예정
 * TODO: 서버 재시작 시 모든 토큰 초기화됨 (임시 저장소 한계)
 */
@Slf4j
@Service
@Primary
public class MemoryTokenStorageService implements TokenStorageService {

    /** Refresh Token 저장소 (Key: userId, Value: RefreshTokenInfo) */
    private final ConcurrentHashMap<Long, RefreshTokenInfo> tokenStore = new ConcurrentHashMap<>();

    /** TTL 만료 체크 스케줄러 */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 서비스 초기화 - TTL 만료 체크 스케줄러 시작
     */
    @PostConstruct
    public void init() {
        // 1분마다 만료된 토큰 정리
        scheduler.scheduleAtFixedRate(this::cleanExpiredTokens, 1, 1, TimeUnit.MINUTES);
        log.info("MemoryTokenStorageService 초기화 완료 - TTL 자동 정리 시작");
    }

    /**
     * 서비스 종료 - 스케줄러 정리
     */
    @PreDestroy
    public void destroy() {
        scheduler.shutdown();
        log.info("MemoryTokenStorageService 종료 - 스케줄러 정리 완료");
    }

    /**
     * Refresh Token 저장
     */
    @Override
    public void saveRefreshToken(Long userId, String refreshToken, long ttlMillis) {
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(ttlMillis * 1_000_000);
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(refreshToken, expiresAt);
        tokenStore.put(userId, tokenInfo);
        log.debug("Refresh Token 저장 완료 - userId: {}, expiresAt: {}", userId, expiresAt);
    }

    /**
     * Refresh Token 조회
     */
    @Override
    public String getRefreshToken(Long userId) {
        RefreshTokenInfo tokenInfo = tokenStore.get(userId);

        if (tokenInfo == null) {
            log.debug("Refresh Token 없음 - userId: {}", userId);
            return null;
        }

        // 만료 확인
        if (tokenInfo.isExpired()) {
            tokenStore.remove(userId);
            log.debug("Refresh Token 만료됨 - userId: {}", userId);
            return null;
        }

        return tokenInfo.getRefreshToken();
    }

    /**
     * Refresh Token 삭제 (단일)
     */
    @Override
    public void deleteRefreshToken(Long userId) {
        tokenStore.remove(userId);
        log.debug("Refresh Token 삭제 완료 - userId: {}", userId);
    }

    /**
     * 사용자별 모든 Refresh Token 삭제
     */
    @Override
    public void deleteAllRefreshTokens(Long userId) {
        // 현재 구현에서는 userId당 하나의 토큰만 저장
        deleteRefreshToken(userId);
        log.debug("모든 Refresh Token 삭제 완료 - userId: {}", userId);
    }

    /**
     * Refresh Token 존재 여부 확인
     */
    @Override
    public boolean existsRefreshToken(Long userId) {
        return getRefreshToken(userId) != null;
    }

    /**
     * 만료된 토큰 정리 (스케줄러)
     */
    private void cleanExpiredTokens() {
        int beforeSize = tokenStore.size();
        tokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int afterSize = tokenStore.size();

        if (beforeSize != afterSize) {
            log.info("만료된 Refresh Token 정리 완료 - 삭제: {}개, 남은: {}개",
                    beforeSize - afterSize, afterSize);
        }
    }

    /**
     * Refresh Token 정보 내부 클래스
     */
    private static class RefreshTokenInfo {
        private final String refreshToken;
        private final LocalDateTime expiresAt;

        public RefreshTokenInfo(String refreshToken, LocalDateTime expiresAt) {
            this.refreshToken = refreshToken;
            this.expiresAt = expiresAt;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}