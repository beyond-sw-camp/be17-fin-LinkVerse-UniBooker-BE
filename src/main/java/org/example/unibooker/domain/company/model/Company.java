package org.example.unibooker.domain.company.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.common.BaseEntity;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("기업")
public class Company extends BaseEntity {

    @Column(nullable = false, length = 100)
    @Comment("기업명")
    private String companyName;

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
    public Company(String companyName, String logoUrl, CompanyStatus status) {
        this.companyName = companyName;
        this.logoUrl = logoUrl;
        this.status = status != null ? status : CompanyStatus.PENDING;
    }

    // 비즈니스 로직 메서드
    public void approve(Long approvedBy) {
        this.status = CompanyStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
        this.rejectionReason = null;
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

    // 상태 확인 메서드
    public boolean isPending() {
        return this.status == CompanyStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == CompanyStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == CompanyStatus.REJECTED;
    }
}
