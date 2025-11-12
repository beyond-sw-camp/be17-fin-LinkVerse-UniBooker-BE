package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CategoryFieldDto;
import org.example.apiresource.domain.model.entity.CategoryFieldDefinitions;

import java.util.List;
import java.util.Optional;

public interface CategoryFieldPersistencePort {

    void save(CategoryFieldDefinitions field);

    // 카테고리 필드 전체 조회
    List<CategoryFieldDefinitions> findByDeletedAtIsNull();

    // 카테고리 필드 단일 조회
    Optional<CategoryFieldDefinitions> findByIdAndDeletedAtIsNull(Long categoryFieldId);

    // 카테고리 별 목록 조회
    List<CategoryFieldDefinitions> findByCategoryAndNotDeleted(ServiceCategory category);
}
