package org.example.apiresource.adapter.out.repository;

import jakarta.persistence.LockModeType;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.entity.Resources;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
    // 수정용 단건 조회 (삭제되지 않은 리소스)
    Optional<Resources> findByIdAndDeletedAtIsNull(Long resourceId);

    // 목록 조회 (활성화 & 미삭제 상태만)
    List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceGroupId);

    // 상세 조회 (활성화 & 미삭제 상태만)
    Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId);

    // 목록 조회 (모든 리소스)
    List<Resources> findAllByResourceGroupId(Long resourceGroupId);

    // 특정 리소스 그룹의 리소스 수 조회(활성화 & 미삭제 상태만)
    @Query("SELECT COUNT(r) FROM Resources r WHERE r.resourceGroup.companyId = :companyId AND r.isActive = true AND r.deletedAt IS NULL")
    int countActiveResourcesByCompanyId(Long companyId);

    int countByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long id);

    int countAllByIsActiveIsTrueAndResourceGroup_Category(ServiceCategory category);

    int countAllByIsActive(Boolean isActive);

    List<Resources> findByDeletedAtIsNullAndStartDateIsNotNullAndEndDateIsNotNull();

    // 상세 조회 (활성화 & 미삭제 상태 & 비관적 락)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Resources r WHERE r.id = :resourceId AND r.isActive = true AND r.deletedAt IS NULL")
    Optional<Resources> findByIdForUpdate(Long resourceId);
}
