package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
