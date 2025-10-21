package org.example.unibooker.domain.resource.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.CategoryFieldDefinitions;
import org.example.unibooker.domain.resource.model.CategoryFieldDto;
import org.example.unibooker.domain.resource.repository.CategoryFieldDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
