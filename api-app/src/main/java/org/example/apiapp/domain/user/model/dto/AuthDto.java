package org.example.apiapp.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공통 인증 관련 DTO 모음
 * - 로그아웃 응답
 * - 토큰 갱신 (권한별 공통)
 */
public class AuthDto {

    /**
     * 로그아웃 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그아웃 응답")
    public static class LogoutResponse {
        @Schema(description = "결과 메시지", example = "로그아웃이 완료되었습니다.")
        private String message;
    }
}