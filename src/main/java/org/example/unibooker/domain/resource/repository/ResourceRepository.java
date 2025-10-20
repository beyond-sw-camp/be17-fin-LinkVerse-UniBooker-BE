package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.Resources;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
    // 수정용 단건 조회 (삭제되지 않은 리소스)
    Optional<Resources> findByIdAndDeletedAtIsNull(Long resourceId);

    // 목록 조회 (활성화 & 미삭제 상태만)
    List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceGroupId);

    // 상세 조회 (활성화 & 미삭제 상태만)
    Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId);
}
