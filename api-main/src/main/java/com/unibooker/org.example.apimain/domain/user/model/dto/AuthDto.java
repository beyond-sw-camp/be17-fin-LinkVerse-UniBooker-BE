package com.unibooker.main.domain.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.unibooker.common.enums.UserRole;

/**
 * 인증 관련 DTO
 */
public class AuthDto {

    /**
     * 로그인 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
        private Long companyId;  // USER용 (선택)
    }

    /**
     * 로그인 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String email;
        private String name;
        private UserRole role;
        private Long companyId;
    }

    /**
     * 회원가입 요청 (일반 사용자)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {
        private String email;
        private String password;
        private String name;
        private String phone;
        private String birthDate;
        private String gender;
        private Long companyId;
    }

    /**
     * 관리자 회원가입 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminSignUpRequest {
        private String email;
        private String password;
        private String name;
        private String phone;
        private String businessNumber;
        private String companyName;
        private String companySlug;
    }

    /**
     * 회원가입 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpResponse {
        private Long userId;
        private String email;
        private String name;
        private UserRole role;
        private String message;
    }

    /**
     * 토큰 갱신 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

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