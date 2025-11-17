package org.example.apiapp.domain.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공통 인증 관련 DTO
 * - 로그아웃 응답
 *
 * [변경 이력]
 * - 레거시 로그인/회원가입 DTO 제거 (권한별 DTO로 분리)
 * - LogoutResponse만 공통 기능으로 유지
 */
public class AuthDto {

    /**
     * 로그아웃 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        /**
         * 성공 메시지
         */
        private String message;
    }
}