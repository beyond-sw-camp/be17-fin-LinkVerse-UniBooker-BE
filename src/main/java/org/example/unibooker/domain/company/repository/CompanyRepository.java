package org.example.unibooker.domain.company.repository;

import org.example.unibooker.domain.company.model.Company;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * 기업명으로 조회
     */
    Optional<Company> findByCompanyName(String companyName);

    /**
     * 상태별 기업 목록 조회
     */
    List<Company> findByStatus(CompanyStatus status);

    /**
     * 기업명 존재 여부 확인
     */
    boolean existsByCompanyName(String companyName);
}