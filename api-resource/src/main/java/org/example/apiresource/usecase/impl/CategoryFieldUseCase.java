package org.example.apiresource.usecase.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.ServiceCategory;
import org.example.apiresource.domain.model.dto.CategoryFieldDto;
import org.example.apiresource.domain.model.entity.CategoryFieldDefinitions;
import org.example.apiresource.domain.service.CategoryFieldService;
import org.example.apiresource.usecase.port.in.CategoryFieldWebPort;
import org.example.apiresource.usecase.port.out.CategoryFieldPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryFieldUseCase implements CategoryFieldWebPort {

    private final CategoryFieldService categoryFieldService;
    private final CategoryFieldPersistencePort categoryFieldPersistencePort;


    // 카테고리 필드 생성
    @Override
    @Transactional
    public void create(CategoryFieldDto.CategoryFieldReq dto) {
        CategoryFieldDefinitions field = categoryFieldService.toEntity(dto);
        categoryFieldPersistencePort.save(field);
    }


    // 카테고리 필드 전체 목록 조회
    @Override
    @Transactional
    public CategoryFieldDto.CategoryFieldListRes getAll() {
        List<CategoryFieldDefinitions> fields = categoryFieldPersistencePort.findByDeletedAtIsNull();

        return categoryFieldService.toCategoryFieldList(fields);
    }


    // 카테고리 필드 단일 조회
    @Override
    @Transactional
    public CategoryFieldDto.CategoryFieldDetailRes getDetail(Long categoryFieldId) {
        CategoryFieldDefinitions field = categoryFieldPersistencePort.findByIdAndDeletedAtIsNull(categoryFieldId)
                .orElseThrow(() -> new EntityNotFoundException("해당 필드를 찾을 수 없습니다."));

        return categoryFieldService.fromEntity(field);
    }


    // 카테고리 별 목록 조회
    @Override
    @Transactional
    public CategoryFieldDto.CategoryFieldListRes getByCategory(String categoryName) {
        // 문자열 → Enum
        ServiceCategory category = ServiceCategory.valueOf(categoryName.toUpperCase());

        List<CategoryFieldDefinitions> fields = categoryFieldPersistencePort.findByCategoryAndNotDeleted(category);

        return categoryFieldService.toCategoryFieldList(fields);
    }


    // 카테고리 필드 수정
    @Override
    @Transactional
    public void update(Long categoryFieldId, CategoryFieldDto.CategoryFieldReq dto) {
        CategoryFieldDefinitions field = categoryFieldPersistencePort.findByIdAndDeletedAtIsNull(categoryFieldId)
                .orElseThrow(() -> new IllegalArgumentException("해당 필드를 찾을 수 없습니다. ID: " + categoryFieldId));

        field.update(dto);
    }


    // 카테고리 필드 삭제
    @Override
    @Transactional
    public void delete(Long categoryFieldId) {
        CategoryFieldDefinitions field = categoryFieldPersistencePort.findByIdAndDeletedAtIsNull(categoryFieldId)
                .orElseThrow(() -> new IllegalArgumentException("해당 필드를 찾을 수 없습니다. ID: " + categoryFieldId));

        field.softDelete();
    }
}
