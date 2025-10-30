package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.user.model.Gender;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    // ========== 관리자 로그인 Request ==========

    /**
     * 관리자/매니저 로그인 요청 DTO
     * - companyId 없이 이메일로만 조회
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "관리자/매니저 로그인 요청")
    public static class AdminLoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "이메일", example = "admin@abc.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호", required = true)
        private String password;
    }

    // ========== 관리자 회원가입 신청 Request ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "관리자 회원가입 신청 요청")
    public static class SignUpRequest {

        @NotBlank(message = "사업자등록번호는 필수입니다")
        @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$",
                message = "사업자등록번호 형식이 올바르지 않습니다 (XXX-XX-XXXXX)")
        @Schema(description = "사업자등록번호 (business_number) - XXX-XX-XXXXX 형식",
                example = "123-45-67890", required = true)
        private String businessNumber;

        @NotBlank(message = "기업명은 필수입니다")
        @Size(min = 2, max = 100, message = "기업명은 2~100자여야 합니다")
        @Schema(description = "기업명 (companies.name)", example = "ABC 회사", required = true)
        private String companyName;

        @NotBlank(message = "Company Slug는 필수입니다")
        @Pattern(regexp = "^[a-z0-9-]{3,30}$",
                message = "Company Slug는 소문자, 숫자, 하이픈(-)만 사용 가능하며 3~30자여야 합니다")
        @Schema(description = "Company Slug (companies.slug) - 소문자, 숫자, 하이픈만 허용 (3-30자)",
                example = "abc-company", required = true)
        private String companySlug;

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        @Schema(description = "관리자 이름 (users.name)", example = "김관리", required = true)
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "관리자 이메일 (users.email)", example = "admin@abc.com", required = true)
        private String email;

        @NotBlank(message = "연락처는 필수입니다")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
        @Schema(description = "관리자 연락처 (users.phone) - 010-XXXX-XXXX 형식",
                example = "010-1234-5678", required = true)
        private String phone;

        @Schema(description = "기업 로고 URL", example = "https://d2h9e9y86awp4t.cloudfront.net/company-logo/201225844.jpg")
        private String logoUrl;

        // 관리자도 생년월일, 성별 필요 시 추가
//        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
//                message = "생년월일 형식이 올바르지 않습니다 (YYYY-MM-DD)")
//        @Schema(description = "생년월일 (birth_date) - YYYY-MM-DD 형식",
//                example = "1990-01-15")
//        private String birthDate;
//
//        @Schema(description = "성별 (gender)", example = "MALE")
//        private Gender gender;
    }

    // ========== 관리자 회원가입 신청 Response ==========

    @Getter
    @Builder
    @Schema(description = "관리자 회원가입 신청 응답")
    public static class SignUpResponse {

        @Schema(description = "처리 결과 메시지",
                example = "회원가입 신청이 완료되었습니다. 승인까지 약 3일 소요됩니다.")
        private String message;

        @Schema(description = "관리자 이메일 (users.email)", example = "admin@abc.com")
        private String email;

        @Schema(description = "기업명 (companies.name)", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug (companies.slug)", example = "abc-company")
        private String companySlug;

        @Schema(description = "서비스 접속 URL", example = "https://abc-company.unibooker.com")
        private String serviceUrl;

        @Schema(description = "승인 예상 소요 일수", example = "3")
        private Integer estimatedDays;
    }

    // ========== 승인 상태 조회 Response ==========

    @Getter
    @Builder
    @Schema(description = "관리자 회원가입 승인 상태 조회 응답")
    public static class StatusResponse {

        @Schema(description = "기업 승인 상태 (companies.status)", example = "PENDING")
        private CompanyStatus status;

        @Schema(description = "기업명 (companies.name)", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug (companies.slug)", example = "abc-company")
        private String companySlug;

        @Schema(description = "관리자 이메일 (users.email)", example = "admin@abc.com")
        private String email;

        @Schema(description = "거절 사유 (companies.rejection_reason)", example = "서류 미비")
        private String rejectionReason;

        @Schema(description = "신청 일시 (companies.created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime appliedAt;
    }

    // ========== 관리자+매니저 목록 조회 Response (신규) ==========

    /**
     * 슈퍼관리자가 관리자+매니저 목록 조회 응답 DTO (페이징)
     */
    @Getter
    @Builder
    @Schema(description = "관리자+매니저 목록 조회 응답 (SUPER_ADMIN 전용)")
    public static class AdminListResponse {

        @Schema(description = "관리자/매니저 목록")
        private List<AdminInfo> admins;

        @Schema(description = "전체 항목 수", example = "50")
        private Long totalElements;

        @Schema(description = "전체 페이지 수", example = "5")
        private Integer totalPages;

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지당 항목 수", example = "10")
        private Integer pageSize;

        /**
         * 관리자/매니저 개별 정보
         */
        @Getter
        @Builder
        @Schema(description = "관리자/매니저 정보")
        public static class AdminInfo {

            @Schema(description = "사용자 ID (users.user_id)", example = "5")
            private Long userId;

            @Schema(description = "이름 (users.name)", example = "김관리")
            private String name;

            @Schema(description = "이메일 (users.email)", example = "admin@abc.com")
            private String email;

            @Schema(description = "연락처 (users.phone)", example = "010-1234-5678")
            private String phone;

            @Schema(description = "권한 (users.role)", example = "ADMIN")
            private UserRole role;

            @Schema(description = "계정 상태 (users.status)", example = "ACTIVE")
            private UserStatus status;

            @Schema(description = "소속 기업 ID (users.company_id)", example = "10")
            private Long companyId;

            @Schema(description = "소속 기업명 (companies.name)", example = "ABC 회사")
            private String companyName;

            @Schema(description = "Company Slug (companies.slug)", example = "abc-company")
            private String companySlug;

            @Schema(description = "가입 일시 (users.created_at)", example = "2025-10-16T14:30:00")
            private LocalDateTime createdAt;

            @Schema(description = "마지막 로그인 일시 (users.last_login_at)", example = "2025-10-16T15:00:00")
            private LocalDateTime lastLoginAt;
        }
    }

    // ========== 비밀번호 재설정 Request ==========

    /**
     * 비밀번호 재설정 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "비밀번호 재설정 요청")
    public static class PasswordResetRequest {

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        @Schema(description = "현재 비밀번호 (임시 비밀번호)", example = "TempPass123!", required = true)
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다."
        )
        @Schema(description = "새 비밀번호", example = "NewSecure123!@", required = true)
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        @Schema(description = "새 비밀번호 확인", example = "NewSecure123!@", required = true)
        private String confirmPassword;
    }

// ========== 비밀번호 재설정 Response ==========

    /**
     * 비밀번호 재설정 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "비밀번호 재설정 응답")
    public static class PasswordResetResponse {

        @Schema(description = "응답 메시지", example = "비밀번호가 성공적으로 변경되었습니다.")
        private String message;

        @Schema(description = "비밀번호 변경 필요 여부", example = "false")
        private Boolean passwordChangeRequired;
    }

    // ========== 관리자/매니저 상태 변경 Request (신규) ==========

    /**
     * 슈퍼관리자가 관리자/매니저 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "관리자/매니저 상태 변경 요청 (SUPER_ADMIN 전용)")
    public static class AdminStatusUpdateRequest {

        @NotNull(message = "변경할 상태는 필수입니다")
        @Schema(description = "변경할 계정 상태 (users.status)",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE", "DELETED"},
                required = true)
        private UserStatus status;

        @Schema(description = "상태 변경 사유 (선택)", example = "승인 완료")
        private String reason;
    }

    // ========== 기업 로고 업데이트 Response ==========

    /**
     * 기업 로고 업데이트 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "기업 로고 업데이트 응답")
    public static class LogoUpdateResponse {

        @Schema(description = "응답 메시지", example = "기업 로고가 성공적으로 변경되었습니다.")
        private String message;

        @Schema(description = "업데이트된 로고 URL", example = "/company-logo/xxx.jpg")
        private String logoUrl;

        @Schema(description = "업데이트 일시")
        private LocalDateTime updatedAt;
    }
}