package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.CategoryFieldDto;
import org.example.unibooker.domain.resource.model.ResourceGroupDto;
import org.example.unibooker.domain.resource.service.CategoryFieldService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카테고리별 필수 필드 관리", description = "예약/신청 서비스 그룹의 카테고리별로 가지는 필수 필드를 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category-field")
public class CategoryFieldController {
    private final CategoryFieldService categoryFieldService;

    // ---------------- 생성 ----------------
    @Operation(summary = "필드 생성", description = "카테고리에 필요한 필수 입력 필드를 생성합니다.")
    @PostMapping
    public BaseResponse register(@RequestBody CategoryFieldDto.CategoryFieldReq dto) {
        categoryFieldService.create(dto);
        return BaseResponse.success("카테고리 필드가 생성되었습니다.");
    }


    // ---------------- 목록 조회 ----------------
    @Operation(summary = "필드 목록 조회", description = "필수 입력 필드들을 조회합니다.")
    @GetMapping
    public CategoryFieldDto.CategoryFieldListRes getCategoryFields() {
        // TODO: service 호출 후 List<CategoryFieldDto.CategoryFieldDetailRes> 생성
        // 예:
        // List<CategoryFieldDto.CategoryFieldDetailRes> fields = categoryFieldService.getAll();
        // return CategoryFieldDto.CategoryFieldListRes.builder().categoryFields(fields).build();
        return null;
    }


    // ---------------- 단일 조회 ----------------
    @Operation(summary = "필드 단일 조회", description = "특정 필드를 단일 조회합니다.")
    @GetMapping("/{categoryFieldId}")
    public CategoryFieldDto.CategoryFieldDetailRes getCategoryField(@PathVariable Long categoryFieldId) {
        // TODO: service 호출 후 단일 DTO 반환
        // 예: return categoryFieldService.getDetail(categoryFieldId);
        return null;
    }


    // ---------------- 수정 ----------------
    @Operation(summary = "필드 수정", description = "기존 필드를 수정합니다.")
    @PutMapping("/{categoryFieldId}")
    public void update(@PathVariable Long categoryFieldId,
                       @RequestBody CategoryFieldDto.CategoryFieldReq dto) {
        // TODO
    }


    // ---------------- 삭제 ----------------
    @Operation(summary = "필드 삭제", description = "필수 입력 필드를 삭제합니다.")
    @DeleteMapping("/{categoryFieldId}")
    public void delete(@PathVariable Long categoryFieldId) {
        // TODO
    }
}
