package org.example.unibooker.domain.user.service;

/**
 * Refresh Token 저장소 인터페이스
 * - Redis 연동 전까지는 MemoryTokenStorageService 사용
 * - Redis 연동 후 RedisTokenStorageService로 교체 예정
 */
public interface TokenStorageService {

    /**
     * Refresh Token 저장
     */
    void saveRefreshToken(Long userId, String refreshToken, long ttlMillis);

    /**
     * Refresh Token 조회
     */
    String getRefreshToken(Long userId);

    /**
     * Refresh Token 삭제 (단일)
     */
    void deleteRefreshToken(Long userId);

    /**
     * 사용자별 모든 Refresh Token 삭제
     */
    void deleteAllRefreshTokens(Long userId);

    /**
     * Refresh Token 존재 여부 확인
     */
    boolean existsRefreshToken(Long userId);
}