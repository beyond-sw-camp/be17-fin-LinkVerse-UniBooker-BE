package org.example.unibooker.domain.company.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public class CompanyDto {

    // ========== Slug 중복 확인 Response ==========
    @Getter
    @Builder
    public static class SlugCheckResponse {
        private Boolean exists;      // true: 이미 사용 중, false: 사용 가능
        private Boolean available;   // true: 사용 가능, false: 사용 불가
        private String message;      // 결과 메시지
        private String slug;         // 확인한 slug
    }

    // ========== 승인/거절 Request ==========

    @Getter
    @NoArgsConstructor
    public static class ApprovalRequest {

        @Size(max = 500, message = "거절 사유는 500자를 초과할 수 없습니다")
        private String rejectionReason;
    }

    // ========== 승인 대기 목록 Response ==========

    @Getter
    @Builder
    public static class PendingResponse {
        private Long companyId;
        private String companyName;
        private String companySlug;
        private String logoUrl;
        private String adminName;
        private String email;
        private String phone;
        private CompanyStatus status;
        private LocalDateTime createdAt;
    }

    // ========== 기업 상세 조회 Response ==========

    @Getter
    @Builder
    public static class DetailResponse {
        // Company 정보
        private Long companyId;
        private String businessNumber;
        private String companyName;
        private String companySlug;
        private String logoUrl;
        private CompanyStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;
        private Long approvedBy;
        private String rejectionReason;

        // Admin User 정보
        private Long adminId;
        private String adminName;
        private String email;
        private String phone;
        private UserStatus userStatus;
    }

    // ========== 승인/거절 Response ==========

    @Getter
    @Builder
    public static class ApprovalResponse {
        private String message;
        private Long companyId;
        private String companyName;
        private String companySlug;
        private String serviceUrl;
        private CompanyStatus status;
        private LocalDateTime processedAt;
    }

    // ========== 일반 사용자용 공개 정보 Response (추가) ==========

    /**
     * 일반 사용자용 기업 공개 정보
     */
    @Getter
    @Builder
    @Schema(description = "기업 공개 정보 응답 (일반 사용자용)")
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

    // ========== 기업 목록 조회 Response (신규) ==========

    /**
     * 기업 목록 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "기업 목록 응답")
    public static class CompanyListResponse {
        @Schema(description = "기업 목록")
        private List<CompanyInfo> companies;

        @Schema(description = "전체 개수")
        private long totalElements;

        @Schema(description = "전체 페이지 수")
        private int totalPages;

        @Schema(description = "현재 페이지")
        private int currentPage;

        @Schema(description = "페이지 크기")
        private int pageSize;
    }

    /**
     * 기업 기본 정보 DTO
     */
    @Getter
    @Builder
    @Schema(description = "기업 기본 정보")
    public static class CompanyInfo {
        @Schema(description = "기업 ID")
        private Long companyId;

        @Schema(description = "기업명")
        private String companyName;

        @Schema(description = "Company Slug")
        private String companySlug;

        @Schema(description = "로고 URL")
        private String logoUrl;

        @Schema(description = "상태")
        private CompanyStatus status;

        @Schema(description = "관리자 이름")
        private String adminName;

        @Schema(description = "관리자 이메일")
        private String adminEmail;

        @Schema(description = "매니저 수")
        private long managerCount;

        @Schema(description = "일반 사용자 수")
        private long userCount;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @Schema(description = "승인일")
        private LocalDateTime approvedAt;
    }

    // ========== 기업 상태 변경 Request (신규) ==========

    /**
     * 기업 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "기업 상태 변경 요청")
    public static class StatusUpdateRequest {
        @Schema(description = "변경할 상태 (ACTIVE or SUSPENDED만 허용)")
        private CompanyStatus status;
    }

    // ========== 기업 상태 변경 Response (신규) ==========

    /**
     * 기업 상태 변경 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "기업 상태 변경 응답")
    public static class StatusUpdateResponse {
        @Schema(description = "메시지")
        private String message;

        @Schema(description = "기업 ID")
        private Long companyId;

        @Schema(description = "기업명")
        private String companyName;

        @Schema(description = "이전 상태")
        private CompanyStatus oldStatus;

        @Schema(description = "새로운 상태")
        private CompanyStatus newStatus;

        @Schema(description = "변경 일시")
        private LocalDateTime updatedAt;
    }
}