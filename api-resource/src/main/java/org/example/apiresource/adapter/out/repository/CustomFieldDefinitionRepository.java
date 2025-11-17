package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.entity.CustomFieldDefinitions;
import org.example.apiresource.domain.model.entity.ResourceGroups;
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

    List<CustomFieldDefinitions> findAllByIdInAndDeletedAtIsNull(List<Long> list);
}