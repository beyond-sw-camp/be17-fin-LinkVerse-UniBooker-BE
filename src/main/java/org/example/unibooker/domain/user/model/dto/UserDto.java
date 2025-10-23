package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.user.model.Gender;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;

public class UserDto {

    // ========== 일반 사용자 회원가입 Request ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "일반 사용자 회원가입 요청")
    public static class SignUpRequest {

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        @Schema(description = "사용자 이름 (name)", example = "홍길동", required = true)
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "이메일 (email)", example = "user@example.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        @Schema(description = "비밀번호 (password) - 8자 이상, 영문, 숫자, 특수문자 포함",
                example = "Password123!", required = true)
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        @Schema(description = "가입할 기업 ID (company_id)", example = "1", required = true)
        private Long companyId;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
        @Schema(description = "연락처 (phone) - 010-XXXX-XXXX 형식", example = "010-1234-5678")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "생년월일 형식이 올바르지 않습니다 (YYYY-MM-DD)")
        @Schema(description = "생년월일 (birth_date) - YYYY-MM-DD 형식",
                example = "1990-01-15")
        private String birthDate;

        @Schema(description = "성별 (gender)", example = "MALE",
                allowableValues = {"MALE", "FEMALE", "UNDEFINED"})
        private Gender gender;
    }

    // ========== 회원가입 Response ==========

    @Getter
    @Builder
    @Schema(description = "일반 사용자 회원가입 응답")
    public static class SignUpResponse {

        @Schema(description = "사용자 ID (user_id)", example = "1")
        private Long id;

        @Schema(description = "사용자 이름 (name)", example = "홍길동")
        private String name;

        @Schema(description = "이메일 (email)", example = "user@example.com")
        private String email;

        @Schema(description = "소속 기업 ID (company_id)", example = "1")
        private Long companyId;

        @Schema(description = "사용자 권한 (role)", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태 (status)", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "가입 일시 (created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;
    }

    // ========== 비밀번호 변경 Request ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "비밀번호 변경 요청")
    public static class PasswordChangeRequest {

        @NotBlank(message = "현재 비밀번호는 필수입니다")
        @Schema(description = "현재 비밀번호", example = "OldPassword123!", required = true)
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        @Schema(description = "새 비밀번호 (password) - 8자 이상, 영문, 숫자, 특수문자 포함",
                example = "NewPassword123!", required = true)
        private String newPassword;

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!", required = true)
        private String confirmPassword;
    }

    // ========== 로그인 Request ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "로그인 요청")
    public static class LoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "이메일 (email)", example = "user@example.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호 (password)", example = "Password123!", required = true)
        private String password;

        @NotNull(message = "기업 ID는 필수입니다")
        @Schema(description = "기업 ID (company_id)", example = "1", required = true)
        private Long companyId;
    }

    // ========== 로그인 Response ==========

    @Getter
    @Builder
    @Schema(description = "로그인 응답")
    public static class LoginResponse {

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;

        @Schema(description = "사용자 ID (user_id)", example = "1")
        private Long userId;

        @Schema(description = "사용자 이름 (name)", example = "홍길동")
        private String name;

        @Schema(description = "이메일 (email)", example = "user@example.com")
        private String email;

        @Schema(description = "사용자 권한 (role)", example = "USER")
        private UserRole role;

        @Schema(description = "비밀번호 변경 필요 여부 (is_first_login)", example = "false")
        private Boolean passwordChangeRequired;

        @Schema(description = "소속 기업 ID (company_id)", example = "10")
        private Long companyId;

        @Schema(description = "소속 기업 URL Slug (company_slug)", example = "abc-company")
        private String companySlug;
    }

    // ========== 로그아웃 Request (신규) ==========

    /**
     * 로그아웃 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "로그아웃 요청")
    public static class LogoutRequest {

        @NotBlank(message = "리프레시 토큰은 필수입니다")
        @Schema(description = "리프레시 토큰 (refresh_token)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                required = true)
        private String refreshToken;
    }

    // ========== 로그아웃 Response (신규) ==========

    /**
     * 로그아웃 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "로그아웃 응답")
    public static class LogoutResponse {

        @Schema(description = "처리 결과 메시지", example = "로그아웃이 완료되었습니다.")
        private String message;

        @Schema(description = "로그아웃 일시", example = "2025-10-16T15:00:00")
        private LocalDateTime logoutAt;
    }

    // ========== 회원 탈퇴 Request (신규) ==========

    /**
     * 회원 탈퇴 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "회원 탈퇴 요청")
    public static class WithdrawRequest {

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호 확인 (password)",
                example = "Password123!",
                required = true)
        private String password;

        @Schema(description = "탈퇴 사유 (선택)", example = "서비스 이용 불만")
        private String reason;
    }

    // ========== 회원 탈퇴 Response (신규) ==========

    /**
     * 회원 탈퇴 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "회원 탈퇴 응답")
    public static class WithdrawResponse {

        @Schema(description = "처리 결과 메시지",
                example = "회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.")
        private String message;

        @Schema(description = "탈퇴한 사용자 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "탈퇴 일시", example = "2025-10-16T15:30:00")
        private LocalDateTime withdrawnAt;
    }

    // ========== 프로필 조회 Response ==========

    @Getter
    @Builder
    @Schema(description = "사용자 프로필 조회 응답")
    public static class ProfileResponse {

        @Schema(description = "사용자 ID (user_id)", example = "1")
        private Long id;

        @Schema(description = "사용자 이름 (name)", example = "홍길동")
        private String name;

        @Schema(description = "이메일 (email)", example = "user@example.com")
        private String email;

        @Schema(description = "연락처 (phone)", example = "010-1234-5678")
        private String phone;

        @Schema(description = "생년월일 (birth_date)", example = "1990-01-15")
        private String birthDate;

        @Schema(description = "성별 (gender)", example = "MALE")
        private Gender gender;

        @Schema(description = "사용자 권한 (role)", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태 (status)", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "소속 기업 ID (company_id)", example = "10")
        private Long companyId;

        @Schema(description = "소속 기업명 (companies.name)", example = "ABC 회사")
        private String companyName;

        @Schema(description = "사업자등록번호 (companies.number)", example = "123-45-67890")
        private String businessNumber;

        @Schema(description = "첫 로그인 여부 (is_first_login)", example = "false")
        private Boolean isFirstLogin;

        @Schema(description = "가입 일시 (created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정 일시 (updated_at)", example = "2025-10-16T15:00:00")
        private LocalDateTime updatedAt;
    }

    // ========== 프로필 수정 Request ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "사용자 프로필 수정 요청")
    public static class ProfileUpdateRequest {

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        @Schema(description = "사용자 이름 (name)", example = "홍길동", required = true)
        private String name;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$",
                message = "연락처 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
        @Schema(description = "연락처 (phone) - 010-XXXX-XXXX 형식", example = "010-1234-5678")
        private String phone;

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "생년월일 형식이 올바르지 않습니다 (YYYY-MM-DD)")
        @Schema(description = "생년월일 (birth_date) - YYYY-MM-DD 형식",
                example = "1990-01-15")
        private String birthDate;

        @Schema(description = "성별 (gender)", example = "MALE")
        private Gender gender;
    }

    // ========== 계정 정보 Response ==========

    /**
     * 이메일로 가입한 계정 정보
     */
    @Getter
    @Builder
    @Schema(description = "이메일로 가입한 계정 정보")
    public static class AccountInfo {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "소속 기업 ID", example = "10")
        private Long companyId;

        @Schema(description = "소속 기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "권한", example = "USER")
        private UserRole role;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "가입 일시", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;
    }
}