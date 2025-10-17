package org.example.unibooker.domain.company.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;

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
}