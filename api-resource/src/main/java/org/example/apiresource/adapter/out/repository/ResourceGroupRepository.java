package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.entity.ResourceGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceGroupRepository extends JpaRepository<ResourceGroups, Long> {

    // 기업명 중복 체크용
    boolean existsByNameAndCompanyId(String name, Long companyId);

    // 특정 기업의 리소스 그룹 조회 (삭제된 거 제외)
    List<ResourceGroups> findAllByCompanyIdAndDeletedAtIsNull(Long companyId);

    // 특정 기업의 리소스 그룹 조회 (삭제된 거 제외)
    Optional<ResourceGroups> findByIdAndDeletedAtIsNull(Long id);

    // 특정 기업의 리소스 그룹 조회 (활성화 상태 확인)
    List<ResourceGroups> findAllByCompanyIdAndIsActive(Long companyId, Boolean isActive);

    // 조회수 증가
    @Modifying
    @Query("UPDATE ResourceGroups rg SET rg.viewCount = rg.viewCount + 1 WHERE rg.id = :groupId")
    void incrementViewCount(Long groupId);

    /**
     * 특정 기업의 리소스 그룹 수 조회 (삭제되지 않은 것만)
     */
//    @Query("SELECT COUNT(rg) FROM ResourceGroups rg WHERE rg.company.id = :companyId AND rg.deletedAt IS NULL")
//    Long countByCompanyId(@Param("companyId") Long companyId);

    List<ResourceGroups> findAllByCompanyIdAndIsActive(Long companyId, boolean isActive);

    List<ResourceGroups> findAllByIsActiveTrueAndDeletedAtIsNull();

    int countAllByCategoryAndIsActiveTrueAndDeletedAtIsNull(ServiceCategory category);
}