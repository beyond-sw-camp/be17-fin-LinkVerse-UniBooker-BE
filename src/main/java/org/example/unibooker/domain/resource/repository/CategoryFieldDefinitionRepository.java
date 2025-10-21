package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.CategoryFieldDefinitions;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryFieldDefinitionRepository extends JpaRepository<CategoryFieldDefinitions, Long> {

    // 카테고리별 삭제되지 않은 필드 조회
    List<CategoryFieldDefinitions> findByCategoryAndDeletedAtIsNull(ServiceCategory category);

    // 삭제되지 않은 전체 필드 조회
    List<CategoryFieldDefinitions> findByDeletedAtIsNull();

    // 삭제되지 않은 단일 필드 조회
    Optional<CategoryFieldDefinitions> findByIdAndDeletedAtIsNull(Long id);
}
