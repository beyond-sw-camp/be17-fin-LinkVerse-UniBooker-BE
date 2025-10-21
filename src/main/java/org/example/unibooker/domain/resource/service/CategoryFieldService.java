package org.example.unibooker.domain.resource.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.CategoryFieldDefinitions;
import org.example.unibooker.domain.resource.model.CategoryFieldDto;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.example.unibooker.domain.resource.repository.CategoryFieldDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryFieldService {
    private final CategoryFieldDefinitionRepository categoryFieldRepository;


    // -------------------- 카테고리 필드 생성 --------------------
    @Transactional
    public void create(CategoryFieldDto.CategoryFieldReq dto) {
        CategoryFieldDefinitions field = dto.toEntity();
        categoryFieldRepository.save(field);
    }


    // -------------------- 전체 목록 조회 --------------------
    public CategoryFieldDto.CategoryFieldListRes getAll() {
        List<CategoryFieldDefinitions> fields = categoryFieldRepository.findByDeletedAtIsNull();
        return CategoryFieldDto.CategoryFieldListRes.fromEntityList(fields);
    }


    // -------------------- 단일 조회 --------------------
    public CategoryFieldDto.CategoryFieldDetailRes getDetail(Long id) {
        CategoryFieldDefinitions field = categoryFieldRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 필드를 찾을 수 없습니다."));

        return CategoryFieldDto.CategoryFieldDetailRes.fromEntity(field);
    }


    // -------------------- 카테고리 별 목록 조회 --------------------
    public CategoryFieldDto.CategoryFieldListRes getByCategory(String categoryName) {
        ServiceCategory category = ServiceCategory.valueOf(categoryName.toUpperCase());
        List<CategoryFieldDefinitions> fields = categoryFieldRepository.findByCategoryAndDeletedAtIsNull(category);
        return CategoryFieldDto.CategoryFieldListRes.fromEntityList(fields);
    }


    // ---------------- 수정 ----------------
    @Transactional
    public void update(Long categoryFieldId, CategoryFieldDto.CategoryFieldReq dto) {
        CategoryFieldDefinitions field = categoryFieldRepository.findByIdAndDeletedAtIsNull(categoryFieldId)
                .orElseThrow(() -> new IllegalArgumentException("해당 필드를 찾을 수 없습니다. ID: " + categoryFieldId));

        field.update(dto);
    }


    // ---------------- 삭제 ----------------
    @Transactional
    public void delete(Long categoryFieldId) {
        CategoryFieldDefinitions field = categoryFieldRepository.findByIdAndDeletedAtIsNull(categoryFieldId)
                .orElseThrow(() -> new IllegalArgumentException("해당 필드를 찾을 수 없습니다. ID: " + categoryFieldId));

        field.softDelete();
    }
}
