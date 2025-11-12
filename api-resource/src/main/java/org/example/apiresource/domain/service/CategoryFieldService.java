package org.example.apiresource.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.CategoryFieldDto;
import org.example.apiresource.domain.model.entity.CategoryFieldDefinitions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryFieldService {

    // 카테고리 필드 생성
    public CategoryFieldDefinitions toEntity(CategoryFieldDto.CategoryFieldReq dto) {
        return CategoryFieldDefinitions.builder()
                .fieldName(dto.getFieldName())
                .description(dto.getDescription())
                .dataType(dto.getDataType())
                .category(dto.getCategory())
                .build();
    }

    // 카테고리 필드 전체 조회
    public CategoryFieldDto.CategoryFieldListRes toCategoryFieldList(List<CategoryFieldDefinitions> fields) {
        List<CategoryFieldDto.CategoryFieldDetailRes> list = fields.stream()
                .map(field -> CategoryFieldDto.CategoryFieldDetailRes.builder()
                        .fieldName(field.getFieldName())
                        .description(field.getDescription())
                        .dataType(field.getDataType())
                        .category(field.getCategory())
                        .build()
                ).toList();

        return CategoryFieldDto.CategoryFieldListRes.builder()
                .categoryFields(list)
                .build();
    }

    // 카테고리 필드 단일 조회
    public CategoryFieldDto.CategoryFieldDetailRes fromEntity(CategoryFieldDefinitions field) {
        return CategoryFieldDto.CategoryFieldDetailRes.builder()
                .fieldName(field.getFieldName())
                .description(field.getDescription())
                .dataType(field.getDataType())
                .category(field.getCategory())
                .build();
    }
}
