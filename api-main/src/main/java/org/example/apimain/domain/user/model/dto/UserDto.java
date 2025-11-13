package org.example.apimain.domain.user.model.dto;

import org.example.common.model.Gender;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 DTO (Main-Service 전용)
 */
public class UserDto {

    // ========== 기존 DTO (유지) ==========

    /**
     * 사용자 정보 조회 응답 (기존 CRUD용)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String birthDate;
        private Gender gender;
        private UserRole role;
        private UserStatus status;
        private Long companyId;
        private Boolean isFirstLogin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 사용자 정보 수정 요청 (기존 CRUD용)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private String phone;
        private String birthDate;
        private Gender gender;
    }

    /**
     * 비밀번호 변경 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        @Schema(description = "현재 비밀번호", example = "OldPassword123!", required = true)
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        @Schema(description = "새 비밀번호", example = "NewPassword123!", required = true)
        private String newPassword;

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!", required = true)
        private String confirmPassword;
    }

    // ========== 추가 DTO (UserController용) ==========

    /**
     * 회원가입 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "일반 사용자 회원가입 요청")
    public static class SignUpRequest {
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50)
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        private Long companyId;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식 오류")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식 오류")
        private String birthDate;

        private Gender gender;
    }

    /**
     * 회원가입 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpResponse {
        private Long id;
        private String name;
        private String email;
        private Long companyId;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime createdAt;
    }

    /**
     * 로그인 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        private Long companyId;
    }

    /**
     * 로그인 응답 (토큰 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseWithToken {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String email;
        private String name;
        private UserRole role;
        private Long companyId;
        private String companySlug;
        private Boolean isFirstLogin;

        public LoginResponse toResponse() {
            return LoginResponse.builder()
                    .userId(userId)
                    .name(name)
                    .email(email)
                    .role(role)
                    .passwordChangeRequired(isFirstLogin)
                    .companyId(companyId)
                    .companySlug(companySlug)
                    .build();
        }
    }

    /**
     * 로그인 응답 (토큰 제외)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private Long userId;
        private String name;
        private String email;
        private UserRole role;
        private Boolean passwordChangeRequired;
        private Long companyId;
        private String companySlug;
    }

    /**
     * 현재 사용자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUserResponse {
        private Long id;
        private String email;
        private String name;
        private Long companyId;
        private String companySlug;
        private UserRole role;
        private UserStatus status;
    }

    /**
     * 프로필 조회 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String birthDate;
        private Gender gender;
        private UserRole role;
        private UserStatus status;
        private Long companyId;
        private String companyName;
        private String businessNumber;
        private String logoUrl;
        private Boolean isFirstLogin;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 프로필 수정 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateRequest {
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50)
        private String name;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
        private String birthDate;

        private Gender gender;
    }

    /**
     * 회원 탈퇴 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WithdrawRequest {
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
        private String reason;
    }

    /**
     * 회원 탈퇴 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WithdrawResponse {
        private String message;
        private String email;
        private LocalDateTime withdrawnAt;
    }

    /**
     * 계정 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private Long userId;
        private String email;
        private String name;
        private Long companyId;
        private String companyName;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime createdAt;
    }

    /**
     * 아이디 찾기 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FindEmailRequest {
        @NotBlank(message = "이름을 입력해주세요")
        private String name;

        @NotNull(message = "기업 ID를 입력해주세요")
        private Long companyId;

        private String phone;
        private String birthDate;
    }

    /**
     * 아이디 찾기 응답
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FindEmailResponse {
        private String maskedEmail;
        private LocalDateTime createdAt;
    }
}