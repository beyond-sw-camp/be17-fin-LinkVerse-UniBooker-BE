package org.example.apimain.domain.company.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.example.common.model.CompanyStatus;
import org.example.common.model.UserStatus;

/**
 * 기업 정보 DTO
 * - API 응답용 DTO
 */
public class CompanyDto {

    // ========== Slug 중복 확인 Response ==========

    /**
     * Slug 중복 확인 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Slug 중복 확인 응답")
    public static class SlugCheckResponse {

        @Schema(description = "이미 사용 중 여부 (true: 사용 중, false: 사용 가능)", example = "false")
        private Boolean exists;

        @Schema(description = "사용 가능 여부 (true: 사용 가능, false: 사용 불가)", example = "true")
        private Boolean available;

        @Schema(description = "결과 메시지", example = "사용 가능한 Slug입니다.")
        private String message;

        @Schema(description = "확인한 Slug", example = "company-a")
        private String slug;
    }

    // ========== 승인/거절 Request ==========

    /**
     * 승인/거절 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 승인/거절 요청")
    public static class ApprovalRequest {

        @Schema(description = "거절 사유 (최대 500자)", example = "사업자등록번호 확인 불가")
        @Size(max = 500, message = "거절 사유는 500자를 초과할 수 없습니다")
        private String rejectionReason;
    }

    // ========== 승인 대기 목록 Response ==========

    /**
     * 승인 대기 기업 목록 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "승인 대기 기업 정보")
    public static class PendingResponse {

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "로고 URL", example = "https://example.com/logo.png")
        private String logoUrl;

        @Schema(description = "관리자 이름", example = "홍길동")
        private String adminName;

        @Schema(description = "관리자 이메일", example = "admin@example.com")
        private String email;

        @Schema(description = "관리자 전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "기업 상태", example = "PENDING")
        private CompanyStatus status;

        @Schema(description = "신청 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;
    }

    // ========== 기업 상세 조회 Response ==========

    /**
     * 기업 상세 정보 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 상세 정보")
    public static class DetailResponse {

        // ===== Company 정보 =====

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "사업자등록번호", example = "123-45-67890")
        private String businessNumber;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "로고 URL", example = "https://example.com/logo.png")
        private String logoUrl;

        @Schema(description = "기업 상태", example = "ACTIVE")
        private CompanyStatus status;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "승인 일시", example = "2025-01-03T15:30:00")
        private LocalDateTime approvedAt;

        @Schema(description = "승인자 ID", example = "999")
        private Long approvedBy;

        @Schema(description = "거절 사유", example = "사업자등록번호 확인 불가")
        private String rejectionReason;

        // ===== Admin User 정보 =====

        @Schema(description = "관리자 ID", example = "10")
        private Long adminId;

        @Schema(description = "관리자 이름", example = "홍길동")
        private String adminName;

        @Schema(description = "관리자 이메일", example = "admin@example.com")
        private String email;

        @Schema(description = "관리자 전화번호", example = "010-1234-5678")
        private String phone;

        @Schema(description = "관리자 계정 상태", example = "ACTIVE")
        private UserStatus userStatus;

        // ===== 플랫폼 이용 현황 =====

        @Schema(description = "서비스 그룹 수", example = "5")
        private Long serviceGroupCount;

        @Schema(description = "일반 사용자 수", example = "120")
        private Long userCount;

        @Schema(description = "최근 로그인 일시", example = "2025-11-10T14:20:00")
        private LocalDateTime lastLoginAt;
    }

    // ========== 승인/거절 Response ==========

    /**
     * 기업 승인/거절 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 승인/거절 응답")
    public static class ApprovalResponse {

        @Schema(description = "응답 메시지", example = "기업 승인이 완료되었습니다.")
        private String message;

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "서비스 URL", example = "http://localhost:5173/c/company-a")
        private String serviceUrl;

        @Schema(description = "기업 상태", example = "ACTIVE")
        private CompanyStatus status;

        @Schema(description = "처리 일시", example = "2025-01-03T15:30:00")
        private LocalDateTime processedAt;
    }

    // ========== 일반 사용자용 공개 정보 Response ==========

    /**
     * 일반 사용자용 기업 공개 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 공개 정보 (일반 사용자용)")
    public static class PublicInfoResponse {

        @Schema(description = "기업 ID", example = "1")
        private Long id;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "로고 URL", example = "https://example.com/logo.png")
        private String logoUrl;
    }

    // ========== 기업 목록 조회 Response (슈퍼 관리자용) ==========

    /**
     * 기업 목록 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 목록 응답")
    public static class CompanyListResponse {

        @Schema(description = "기업 목록")
        private List<CompanyInfo> companies;

        @Schema(description = "전체 개수", example = "50")
        private long totalElements;

        @Schema(description = "전체 페이지 수", example = "5")
        private int totalPages;

        @Schema(description = "현재 페이지", example = "0")
        private int currentPage;

        @Schema(description = "페이지 크기", example = "10")
        private int pageSize;
    }

    /**
     * 기업 기본 정보 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 기본 정보")
    public static class CompanyInfo {

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "Company Slug", example = "company-a")
        private String companySlug;

        @Schema(description = "로고 URL", example = "https://example.com/logo.png")
        private String logoUrl;

        @Schema(description = "기업 상태", example = "ACTIVE")
        private CompanyStatus status;

        @Schema(description = "관리자 이름", example = "홍길동")
        private String adminName;

        @Schema(description = "관리자 이메일", example = "admin@example.com")
        private String adminEmail;

        @Schema(description = "매니저 수", example = "3")
        private long managerCount;

        @Schema(description = "일반 사용자 수", example = "120")
        private long userCount;

        @Schema(description = "생성 일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "승인 일시", example = "2025-01-03T15:30:00")
        private LocalDateTime approvedAt;
    }

    // ========== 기업 상태 변경 Request ==========

    /**
     * 기업 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 상태 변경 요청")
    public static class StatusUpdateRequest {

        @Schema(description = "변경할 상태", example = "ACTIVE",
                allowableValues = {"ACTIVE", "SUSPENDED"})
        private CompanyStatus status;
    }

    // ========== 기업 상태 변경 Response ==========

    /**
     * 기업 상태 변경 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "기업 상태 변경 응답")
    public static class StatusUpdateResponse {

        @Schema(description = "응답 메시지", example = "기업 상태가 변경되었습니다.")
        private String message;

        @Schema(description = "기업 ID", example = "1")
        private Long companyId;

        @Schema(description = "기업명", example = "ABC 회사")
        private String companyName;

        @Schema(description = "이전 상태", example = "SUSPENDED")
        private CompanyStatus oldStatus;

        @Schema(description = "새로운 상태", example = "ACTIVE")
        private CompanyStatus newStatus;

        @Schema(description = "변경 일시", example = "2025-11-11T16:30:00")
        private LocalDateTime updatedAt;
    }
}