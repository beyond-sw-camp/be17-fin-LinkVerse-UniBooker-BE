package org.example.unibooker.domain.company.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("기업")
public class Companies extends BaseEntity {

    @Column(nullable = false, length = 100)
    @Comment("기업명")
    private String companyName;

    @Column(nullable = false, unique = true, length = 12)
    @Comment("사업자등록번호")
    private String businessNumber;

    @Column(nullable = false, unique = true, length = 30)
    @Comment("기업 URL Slug")
    private String companySlug;

    @Column(length = 255)
    @Comment("기업 로고 URL")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("승인 상태")
    private CompanyStatus status;

    @Comment("승인 일시")
    private LocalDateTime approvedAt;

    @Comment("승인자 ID")
    private Long approvedBy;

    @Column(length = 500)
    @Comment("거절 사유")
    private String rejectionReason;

    @Builder
    public Companies(String businessNumber, String companyName, String companySlug,
                     String logoUrl, CompanyStatus status) {
        this.businessNumber = businessNumber;
        this.companyName = companyName;
        this.companySlug = companySlug;
        this.logoUrl = logoUrl;
        this.status = status != null ? status : CompanyStatus.PENDING;
    }

    /**
     * 기업 승인 처리
     * - 상태를 ACTIVE로 변경
     */
    public void approve(Long approvedBy) {
        this.status = CompanyStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
        this.rejectionReason = null;
    }

    /**
     * 기업 서비스 정지
     */
    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
    }

    /**
     * 기업 서비스 재개
     */
    public void activate() {
        this.status = CompanyStatus.ACTIVE;
    }

    public void reject(String rejectionReason) {
        this.status = CompanyStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.approvedAt = null;
        this.approvedBy = null;
    }

    public void updateLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getServiceUrl(String baseUrl) { return baseUrl + "/c/" + this.companySlug; }

    public boolean isPending() {
        return this.status == CompanyStatus.PENDING;
    }

    /**
     * 승인(활성) 상태 확인
     */
    public boolean isApproved() {
        return this.status == CompanyStatus.ACTIVE;
    }

    /**
     * 정지 상태 확인
     */
    public boolean isSuspended() {
        return this.status == CompanyStatus.SUSPENDED;
    }

    /**
     * 거절 상태 확인
     */
    public boolean isRejected() {
        return this.status == CompanyStatus.REJECTED;
    }
}
