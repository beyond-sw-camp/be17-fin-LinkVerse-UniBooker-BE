package com.unibooker.main.domain.company.model.entity;

import com.unibooker.common.entity.BaseEntity;
import com.unibooker.common.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기업 엔티티
 */
@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Companies extends BaseEntity {

    /** 기업명 */
    @Column(nullable = false, length = 100)
    private String companyName;

    /** 사업자등록번호 */
    @Column(nullable = false, unique = true, length = 12)
    private String businessNumber;

    /** 기업 URL Slug */
    @Column(nullable = false, unique = true, length = 30)
    private String companySlug;

    /** 기업 로고 URL */
    @Column(length = 255)
    private String logoUrl;

    /** 승인 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status;

    /** 승인 일시 */
    private LocalDateTime approvedAt;

    /** 승인자 ID */
    private Long approvedBy;

    /** 거절 사유 */
    @Column(length = 500)
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

    /**
     * 기업 거절 처리
     */
    public void reject(String rejectionReason) {
        this.status = CompanyStatus.REJECTED;
        this.rejectionReason = rejectionReason;
        this.approvedAt = null;
        this.approvedBy = null;
    }

    /**
     * 로고 URL 수정
     */
    public void updateLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * 기업명 수정
     */
    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * 서비스 URL 생성
     */
    public String getServiceUrl(String baseUrl) {
        return baseUrl + "/c/" + this.companySlug;
    }

    /**
     * 승인 대기 상태 확인
     */
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