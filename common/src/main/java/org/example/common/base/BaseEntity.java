package org.example.common.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 엔티티 베이스 클래스
 * - 모든 엔티티가 상속받아 사용
 * - 공통 필드: id, createdAt, updatedAt, deletedAt
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /** 기본키 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 생성일시 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일시 */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 삭제일시 (Soft Delete) */
    private LocalDateTime deletedAt;

    /**
     * Soft Delete 처리
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Soft Delete 복구
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}