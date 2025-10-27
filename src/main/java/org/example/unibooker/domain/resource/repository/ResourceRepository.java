package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceStatus;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // 특정 리소스 그룹의 리소스 수 조회(활성화 & 미삭제 상태만)
    @Query("SELECT COUNT(r) FROM Resources r WHERE r.resourceGroup.company.id = :companyId AND r.isActive = true AND r.deletedAt IS NULL")
    int countActiveResourcesByCompanyId(Long companyId);



    int countByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long id);

    int countAllByIsActiveIsTrueAndResourceGroup_Category(ServiceCategory category);

    int countAllByIsActive(Boolean isActive);
}
