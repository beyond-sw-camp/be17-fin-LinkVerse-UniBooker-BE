package org.example.unibooker.domain.queue.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대기열 관련 DTO
 * - 대기열 진입, 상태 조회, 입장 응답
 */
public class QueueDto {

    /**
     * 대기열 진입 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinResponse {
        private String token;
        private Long position;
        private Long totalWaiting;
        private String message;
    }

    /**
     * 대기열 상태 조회 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResponse {
        private Long position;
        private Long totalWaiting;
        private Long etaSeconds;
        private Boolean canEnter;
        private String message;
    }

    /**
     * 토큰 소비(입장) 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumeResponse {
        private Boolean success;
        private String message;
        private Long remainingSeconds;
    }
}
