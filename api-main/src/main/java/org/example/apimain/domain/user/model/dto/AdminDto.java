package org.example.apimain.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import org.example.common.model.CompanyStatus;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

/**
 * 관리자(Admin) 관련 DTO 모음
 * - 회원가입, 로그인, 상태 조회
 * - 관리자 목록 조회 (슈퍼 관리자용)
 * - 비밀번호 재설정
 */
public class AdminDto {

    // ========== 관리자 로그인 Request ==========

    /**
     * 관리자/매니저 로그인 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "관리자/매니저 로그인 요청")
    public static class AdminLoginRequest {

        @Schema(description = "이메일", example = "admin@abc.com", required = true)
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @Schema(description = "비밀번호", example = "password123!", required = true)
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    // ========== 관리자 회원가입 신청 Request ==========

    /**
     * 관리자 회원가입 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "관리자 회원가입 신청 요청")
    public static class SignUpRequest {

        @Schema(description = "사업자등록번호", example = "1234567890", required = true)
        @NotBlank(message = "사업자등록번호는 필수입니다")
        @Size(min = 10, max = 12, message = "사업자등록번호는 10~12자여야 합니다")
        private String businessNumber;

        @Schema(description = "기업명", example = "ABC 회사", required = true)
        @NotBlank(message = "기업명은 필수입니다")
        @Size(min = 2, max = 100, message = "기업명은 2~100자여야 합니다")
        private String companyName;

        @Schema(description = "Company Slug (서비스 도메인)", example = "abc-company", required = true)
        @NotBlank(message = "Company Slug는 필수입니다")
        @Size(min = 3, max = 30, message = "Slug는 3~30자여야 합니다")
        private String companySlug;

        @Schema(description = "관리자 이름", example = "홍길동", required = true)
        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        private String name;

        @Schema(description = "관리자 이메일", example = "admin@abc.com", required = true)
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @Schema(description = "관리자 연락처", example = "010-1234-5678", required = true)
        @NotBlank(message = "연락처는 필수입니다")
        @Size(min = 11, max = 13, message = "연락처는 11~13자여야 합니다")
        private String phone;

        @Schema(description = "기업 로고 URL (S3 업로드 후)", example = "https://s3.amazonaws.com/bucket/logo.png")
        private String logoUrl;
    }

    // ========== 관리자 회원가입 신청 Response ==========

    /**
     * 관리자 회원가입 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "관리자 회원가입 신청 응답")
    public static class SignUpResponse {

        @Schema(description = "처리 결과 메시지", example = "회원가입 신청이 완료되었습니다. 승인까지 약 3일 소요됩니다.")
        private String message;

        @Schema(description = "관리자 이메일", example = "admin@abc.com")
        private String email;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "abc-company")
        private String companySlug;

        @Schema(description = "서비스 접속 URL", example = "https://abc-company.unibooker.com")
        private String serviceUrl;

        @Schema(description = "승인 예상 소요 일수", example = "3")
        private Integer estimatedDays;
    }

    // ========== 승인 상태 조회 Response ==========

    /**
     * 관리자 회원가입 승인 상태 조회 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "관리자 회원가입 승인 상태 조회 응답")
    public static class StatusResponse {

        @Schema(description = "기업 승인 상태", example = "PENDING")
        private CompanyStatus status;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "abc-company")
        private String companySlug;

        @Schema(description = "관리자 이메일", example = "admin@abc.com")
        private String email;

        @Schema(description = "거절 사유", example = "서류 미비")
        private String rejectionReason;

        @Schema(description = "신청 일시", example = "2025-10-16T14:30:00")
        private LocalDateTime appliedAt;
    }

    // ========== 관리자+매니저 목록 조회 Response ==========

    /**
     * 슈퍼관리자가 관리자+매니저 목록 조회 응답 DTO (페이징)
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
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
        @AllArgsConstructor
        @NoArgsConstructor
        @Schema(description = "관리자/매니저 정보")
        public static class AdminInfo {

            @Schema(description = "사용자 ID", example = "5")
            private Long userId;

            @Schema(description = "이름", example = "김관리")
            private String name;

            @Schema(description = "이메일", example = "admin@abc.com")
            private String email;

            @Schema(description = "연락처", example = "010-1234-5678")
            private String phone;

            @Schema(description = "권한", example = "ADMIN")
            private UserRole role;

            @Schema(description = "계정 상태", example = "ACTIVE")
            private UserStatus status;

            @Schema(description = "소속 기업 ID", example = "10")
            private Long companyId;

            @Schema(description = "소속 기업명", example = "ABC 회사")
            private String companyName;

            @Schema(description = "Company Slug", example = "abc-company")
            private String companySlug;

            @Schema(description = "가입 일시", example = "2025-10-16T14:30:00")
            private LocalDateTime createdAt;

            @Schema(description = "마지막 로그인 일시", example = "2025-10-16T15:00:00")
            private LocalDateTime lastLoginAt;
        }
    }

    // ========== 관리자/매니저 상태 변경 Request ==========

    /**
     * 관리자/매니저 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "관리자/매니저 상태 변경 요청")
    public static class AdminStatusUpdateRequest {

        @Schema(description = "변경할 계정 상태", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "DELETED"},
                required = true)
        @NotBlank(message = "상태는 필수입니다")
        private UserStatus status;
    }

    // ========== 비밀번호 재설정 Request ==========

    /**
     * 비밀번호 재설정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "비밀번호 재설정 요청")
    public static class PasswordResetRequest {

        @Schema(description = "현재 비밀번호 (임시 비밀번호)", example = "TempPassword123!", required = true)
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @Schema(description = "새 비밀번호", example = "NewPassword123!", required = true)
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;

        @Schema(description = "새 비밀번호 확인", example = "NewPassword123!", required = true)
        @NotBlank(message = "비밀번호 확인은 필수입니다")
        private String confirmPassword;
    }

    // ========== 비밀번호 재설정 Response ==========

    /**
     * 비밀번호 재설정 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "비밀번호 재설정 응답")
    public static class PasswordResetResponse {

        @Schema(description = "처리 결과 메시지", example = "비밀번호가 성공적으로 변경되었습니다.")
        private String message;

        @Schema(description = "비밀번호 변경 필수 여부", example = "false")
        private Boolean passwordChangeRequired;
    }

    // ========== MANAGER → ADMIN 승격 Response ==========

    /**
     * MANAGER → ADMIN 승격 응답 DTO
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "MANAGER ADMIN 승격 응답")
    public static class PromoteResponse {

        @Schema(description = "처리 결과 메시지",
                example = "매니저가 관리자로 승격되었습니다. 서비스가 정상화되었습니다.")
        private String message;

        @Schema(description = "승격된 사용자 ID", example = "15")
        private Long userId;

        @Schema(description = "이메일", example = "manager@company.com")
        private String email;

        @Schema(description = "이름", example = "김매니저")
        private String name;

        @Schema(description = "이전 권한", example = "MANAGER")
        private UserRole oldRole;

        @Schema(description = "새 권한", example = "ADMIN")
        private UserRole newRole;

        @Schema(description = "기업 ID", example = "10")
        private Long companyId;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "이전 기업 상태", example = "ADMIN_PENDING")
        private CompanyStatus oldCompanyStatus;

        @Schema(description = "새 기업 상태", example = "ACTIVE")
        private CompanyStatus newCompanyStatus;

        @Schema(description = "승격 일시", example = "2025-11-14T15:30:00")
        private LocalDateTime promotedAt;
    }
}