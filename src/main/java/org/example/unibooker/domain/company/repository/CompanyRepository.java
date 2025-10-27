package org.example.unibooker.domain.company.repository;

import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}