package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CustomFieldDefinitions;
import org.example.unibooker.domain.resource.model.CustomTargetType;
import org.example.unibooker.domain.resource.model.ResourceGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomFieldDefinitionRepository extends JpaRepository<CustomFieldDefinitions, Long> {

    // 리소스 그룹 내의 삭제되지 않은 커스텀 필드 전체 조회
    List<CustomFieldDefinitions> findByResourceGroupAndDeletedAtIsNull(ResourceGroups resourceGroup);

    // 리소스 그룹 내의 특정 타겟 타입(SERVICE / USER)의 삭제되지 않은 커스텀 필드 조회
    List<CustomFieldDefinitions> findByResourceGroupAndTargetTypeAndDeletedAtIsNull(
            ResourceGroups resourceGroup,
            CustomTargetType targetType
    );

    // 삭제되지 않은 필드 조회
    Optional<CustomFieldDefinitions> findByIdAndDeletedAtIsNull(Long id);
}
