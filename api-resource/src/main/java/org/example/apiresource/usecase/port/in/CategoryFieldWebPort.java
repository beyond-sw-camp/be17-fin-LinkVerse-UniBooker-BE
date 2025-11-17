package org.example.apiresource.usecase.port.in;

import org.example.apiresource.domain.model.dto.CategoryFieldDto;

public interface CategoryFieldWebPort {

    // 카테고리 필드 생성
    void create(CategoryFieldDto.CategoryFieldReq dto);

    // 카테고리 전체 목록 조회
    CategoryFieldDto.CategoryFieldListRes getAll();

    // 카테고리 필드 단일 조회
    CategoryFieldDto.CategoryFieldDetailRes getDetail(Long categoryFieldId);

    // 카테고리 별 목록 조회
    CategoryFieldDto.CategoryFieldListRes getByCategory(String category);

    // 카테고리 필드 수정
    void update(Long categoryFieldId, CategoryFieldDto.CategoryFieldReq dto);

    // 카테고리 필드 삭제
    void delete(Long categoryFieldId);
}