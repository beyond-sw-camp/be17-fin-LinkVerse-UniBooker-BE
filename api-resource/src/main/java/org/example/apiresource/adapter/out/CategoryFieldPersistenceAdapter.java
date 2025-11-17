package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.CategoryFieldDefinitionRepository;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CategoryFieldDto;
import org.example.apiresource.domain.model.entity.CategoryFieldDefinitions;
import org.example.apiresource.usecase.port.out.CategoryFieldPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryFieldPersistenceAdapter implements CategoryFieldPersistencePort {

    private final CategoryFieldDefinitionRepository categoryFieldDefinitionRepository;

    // 카테고리 필드 생성
    @Override
    @Transactional
    public void save(CategoryFieldDefinitions field) {
        categoryFieldDefinitionRepository.save(field);
    }


    // 카테고리 필드 전체 조회
    @Override
    @Transactional
    public List<CategoryFieldDefinitions> findByDeletedAtIsNull() {
        return categoryFieldDefinitionRepository.findByDeletedAtIsNull();
    }


    // 카테고리 필드 단일 조회
    @Override
    @Transactional
    public Optional<CategoryFieldDefinitions> findByIdAndDeletedAtIsNull(Long categoryFieldId) {
        return categoryFieldDefinitionRepository.findByIdAndDeletedAtIsNull(categoryFieldId);
    }


    // 카테고리 별 목록 조회
    @Override
    @Transactional
    public List<CategoryFieldDefinitions> findByCategoryAndNotDeleted(ServiceCategory category) {
        return categoryFieldDefinitionRepository.findByCategoryAndDeletedAtIsNull(category);
    }
}