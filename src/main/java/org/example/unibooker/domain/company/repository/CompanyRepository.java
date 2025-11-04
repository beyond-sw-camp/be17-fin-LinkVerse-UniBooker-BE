package org.example.unibooker.domain.company.repository;

import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Companies, Long> {

    /**
     * 기업명으로 조회
     */
    Optional<Companies> findByCompanyName(String companyName);

    /**
     * 상태별 기업 목록 조회
     */
    List<Companies> findByStatus(CompanyStatus status);

    /**
     * 기업명 존재 여부 확인
     */
    boolean existsByCompanyName(String companyName);

    /**
     * 사업자등록번호로 조회
     */
    Optional<Companies> findByBusinessNumber(String businessNumber);

    /**
     * 사업자등록번호 존재 여부 확인
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * companySlug로 조회
     */
    Optional<Companies> findByCompanySlug(String companySlug);

    /**
     * companySlug 존재 여부 확인
     */
    boolean existsByCompanySlug(String companySlug);

    // 기간별 가입한 기업의 수 조회
    int countAllByStatusAndApprovedAtBetween(CompanyStatus status, LocalDateTime start, LocalDateTime end);

    /**
     * 여러 상태로 기업 조회 (페이징)
     */
    Page<Companies> findByStatusIn(List<CompanyStatus> statuses, Pageable pageable);

    /**
     * 기업 검색 (상태 + 키워드 + 페이징)
     */
    @Query("SELECT c FROM Companies c WHERE " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:keyword IS NULL OR c.companyName LIKE %:keyword% OR c.companySlug LIKE %:keyword%)")
    Page<Companies> searchCompanies(
            @Param("status") CompanyStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 기업 검색 (PENDING 제외 + 키워드 + 페이징)
     * - 기업관리 목록용: ACTIVE, SUSPENDED만 조회
     */
    @Query("SELECT c FROM Companies c WHERE " +
            "c.status IN ('ACTIVE', 'SUSPENDED') AND " +
            "(:keyword IS NULL OR c.companyName LIKE %:keyword% OR c.companySlug LIKE %:keyword%)")
    Page<Companies> searchCompaniesExcludingPending(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * REJECTED 상태 기업 중 일정 시간 경과한 기업 조회
     * - 배치 작업용: 자동 삭제 대상 조회
     */
    List<Companies> findByStatusAndCreatedAtBefore(CompanyStatus status, LocalDateTime createdAtBefore);
}