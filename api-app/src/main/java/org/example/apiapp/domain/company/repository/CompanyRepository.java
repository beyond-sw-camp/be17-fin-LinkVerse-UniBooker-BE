package org.example.apiapp.domain.company.repository;

import org.example.apiapp.domain.company.model.entity.Companies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.common.model.CompanyStatus;

/**
 * 기업 레포지토리
 */
@Repository
public interface CompanyRepository extends JpaRepository<Companies, Long> {

    // ========== 기본 조회 (삭제 제외) ==========

    /**
     * Slug로 기업 조회
     */
    Optional<Companies> findByCompanySlugAndDeletedAtIsNull(String companySlug);

    /**
     * 사업자등록번호로 기업 조회
     */
    Optional<Companies> findByBusinessNumberAndDeletedAtIsNull(String businessNumber);

    /**
     * ID로 조회 (삭제되지 않은 것만)
     */
    Optional<Companies> findByIdAndDeletedAtIsNull(Long id);

    // ========== 중복 확인 (삭제 포함) ==========

    /**
     * 사업자등록번호 중복 확인 (삭제 포함)
     * - SignUp에서 사업자등록번호 중복 체크용
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * Slug 중복 확인 (삭제 포함)
     * - SignUp에서 Slug 중복 체크용
     */
    boolean existsByCompanySlug(String companySlug);

    /**
     * 사업자등록번호 중복 확인 (삭제 제외)
     */
    boolean existsByBusinessNumberAndDeletedAtIsNull(String businessNumber);

    /**
     * Slug 중복 확인 (삭제 제외)
     */
    boolean existsByCompanySlugAndDeletedAtIsNull(String companySlug);

    // ========== 상태별 조회 ==========

    /**
     * 상태별 기업 목록 조회 (삭제 포함)
     * - Approval에서 승인 대기 기업 조회용
     */
    List<Companies> findByStatus(CompanyStatus status);

    /**
     * 상태별 기업 목록 조회 (삭제 제외)
     */
    List<Companies> findByStatusAndDeletedAtIsNull(CompanyStatus status);

    // ========== 페이징 조회 (SuperService, CompanyService용) ==========

    /**
     * 여러 상태로 기업 목록 조회 (삭제 제외, 페이징)
     */
    Page<Companies> findByStatusInAndDeletedAtIsNull(
            List<CompanyStatus> statuses,
            Pageable pageable);

    /**
     * 여러 상태 + 키워드 검색 (삭제 제외, 페이징)
     */
    Page<Companies> findByStatusInAndCompanyNameContainingAndDeletedAtIsNull(
            List<CompanyStatus> statuses,
            String keyword,
            Pageable pageable);

    /**
     * 상태별 기업 목록 조회 (삭제 제외, 페이징)
     */
    Page<Companies> findByStatusAndDeletedAtIsNull(
            CompanyStatus status,
            Pageable pageable);

    /**
     * 상태 + 키워드 검색 (삭제 제외, 페이징)
     */
    Page<Companies> findByStatusAndCompanyNameContainingAndDeletedAtIsNull(
            CompanyStatus status,
            String keyword,
            Pageable pageable);

    // ========== 배치 작업용 ==========

    /**
     * 특정 상태 + 생성일 기준 기업 조회
     * - 배치 작업: 거절된 기업 자동 삭제용
     */
    List<Companies> findByStatusAndCreatedAtBefore(
            CompanyStatus status,
            LocalDateTime cutoffTime);


    @Query("""
       SELECT COUNT(c) 
       FROM Companies c 
       WHERE YEAR(c.approvedAt) = :year
       """)
    long countCompaniesByYear(@Param("year") int year);

    long countAllByStatus(CompanyStatus companyStatus);

    @Query("SELECT COUNT(u) FROM Users u WHERE u.companyId = :companyId")
    int countUsersByCompanyId(@Param("companyId") Long companyId);
}