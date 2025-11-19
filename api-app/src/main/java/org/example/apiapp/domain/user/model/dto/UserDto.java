package org.example.apiapp.domain.user.model.dto;

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
 * 일반 사용자 관련 DTO 모음
 * - 회원가입, 로그인, 프로필 관리
 * - 비밀번호 변경, 회원 탈퇴
 * - 이메일 찾기, 계정 정보 조회
 */
public class UserDto {

    /**
     * 사용자 정보 조회 응답 (기존 CRUD용)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 정보 조회 응답")
    public static class Response {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "첫 로그인 여부", example = "false")
        private Boolean isFirstLogin;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시", example = "2025-01-02T10:00:00")
        private LocalDateTime updatedAt;
    }

    /**
     * 사용자 정보 수정 요청 (기존 CRUD용)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자 정보 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;
    }

    /**
     * 비밀번호 변경 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 변경 요청")
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
        @Schema(description = "이름", example = "홍길동", required = true)
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "이메일", example = "user@example.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        @Schema(description = "비밀번호", example = "Password123!", required = true)
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        @Schema(description = "기업 ID", example = "1", required = true)
        private Long companyId;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식 오류")
        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일 형식 오류")
        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;
    }

    /**
     * 회원가입 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원가입 응답")
    public static class SignUpResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;
    }

    /**
     * 로그인 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 요청")
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email
        @Schema(description = "이메일", example = "user@example.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호", example = "Password123!", required = true)
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        @Schema(description = "기업 ID", example = "1", required = true)
        private Long companyId;
    }

    /**
     * 로그인 응답 (토큰 포함)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "로그인 응답 (토큰 포함)")
    public static class LoginResponseWithToken {
        @Schema(description = "Access Token")
        private String accessToken;

        @Schema(description = "Refresh Token")
        private String refreshToken;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "첫 로그인 여부", example = "false")
        private Boolean isFirstLogin;

        /**
         * 토큰 제외한 응답 DTO로 변환
         */
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
    @Schema(description = "로그인 응답 (토큰 제외)")
    public static class LoginResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "비밀번호 변경 필요 여부", example = "false")
        private Boolean passwordChangeRequired;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;
    }

    /**
     * 현재 사용자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "현재 사용자 정보")
    public static class CurrentUserResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;
    }

    /**
     * 프로필 조회 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 조회 응답")
    public static class ProfileResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "소속 기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "사업자등록번호", example = "123-45-67890")
        private String businessNumber;

        @Schema(description = "기업 로고 URL", example = "https://example.com/logo.png")
        private String logoUrl;

        @Schema(description = "첫 로그인 여부", example = "false")
        private Boolean isFirstLogin;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시", example = "2025-01-02T10:00:00")
        private LocalDateTime updatedAt;
    }

    /**
     * 프로필 수정 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 수정 요청")
    public static class ProfileUpdateRequest {
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50)
        @Schema(description = "이름", example = "홍길동", required = true)
        private String name;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;
    }

    /**
     * 회원 탈퇴 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 탈퇴 요청")
    public static class WithdrawRequest {
        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호", example = "Password123!", required = true)
        private String password;

        @Schema(description = "탈퇴 사유", example = "서비스 이용 불편")
        private String reason;
    }

    /**
     * 회원 탈퇴 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원 탈퇴 응답")
    public static class WithdrawResponse {
        @Schema(description = "결과 메시지", example = "회원 탈퇴가 완료되었습니다.")
        private String message;

        @Schema(description = "탈퇴한 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "탈퇴 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime withdrawnAt;
    }

    /**
     * 계정 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "계정 정보")
    public static class AccountInfo {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "소속 기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "소속 기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;
    }

    /**
     * 아이디 찾기 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "아이디 찾기 요청")
    public static class FindEmailRequest {
        @NotBlank(message = "이름을 입력해주세요")
        @Schema(description = "이름", example = "홍길동", required = true)
        private String name;

        @NotNull(message = "기업 ID를 입력해주세요")
        @Schema(description = "기업 ID", example = "1", required = true)
        private Long companyId;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "생년월일", example = "1990-01-01")
        private String birthDate;
    }

    /**
     * 아이디 찾기 응답
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "아이디 찾기 응답")
    public static class FindEmailResponse {
        @Schema(description = "마스킹 처리된 이메일", example = "us***@example.com")
        private String maskedEmail;

        @Schema(description = "계정 생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;
    }
}