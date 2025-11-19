package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiresource.domain.model.dto.CategoryFieldDto;
import org.example.apiresource.usecase.port.in.CategoryFieldWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리별 필수 필드 관리 컨트롤러
 * - 카테고리 필수 필드 생성, 조회, 수정, 삭제
 * - 카테고리별 필드 목록 조회
 * - 예약/신청 서비스의 카테고리별 필수 입력 필드 관리
 */
@Slf4j
@Tag(name = "Category Field API", description = "카테고리별 필수 필드 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category-field")
public class CategoryFieldWebAdapter {

    /** 카테고리 필드 웹 포트 */
    private final CategoryFieldWebPort categoryFieldWebPort;

    /**
     * 카테고리 필수 필드 생성
     */
    @Operation(
            summary = "카테고리 필수 필드 생성",
            description = "특정 카테고리에 필요한 필수 입력 필드를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping
    public BaseResponse register(@RequestBody CategoryFieldDto.CategoryFieldReq dto) {
        log.info("카테고리 필드 생성 요청 - category: {}", dto.getCategory());
        categoryFieldWebPort.create(dto);
        return BaseResponse.success("카테고리 필드가 생성되었습니다.");
    }

    /**
     * 모든 카테고리 필드 목록 조회
     */
    @Operation(
            summary = "카테고리 필드 목록 조회",
            description = "모든 카테고리의 필수 입력 필드 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping
    public BaseResponse<CategoryFieldDto.CategoryFieldListRes> getCategoryFields() {
        log.info("카테고리 필드 목록 조회");
        CategoryFieldDto.CategoryFieldListRes response = categoryFieldWebPort.getAll();
        return BaseResponse.success(response);
    }

    /**
     * 카테고리 필드 상세 조회
     */
    @Operation(
            summary = "카테고리 필드 상세 조회",
            description = "특정 카테고리 필드의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드를 찾을 수 없음")
            }
    )
    @GetMapping("/{categoryFieldId}")
    public BaseResponse<CategoryFieldDto.CategoryFieldDetailRes> getCategoryField(
            @Parameter(description = "카테고리 필드 ID", required = true, example = "1")
            @PathVariable Long categoryFieldId) {

        log.info("카테고리 필드 상세 조회 - categoryFieldId: {}", categoryFieldId);
        CategoryFieldDto.CategoryFieldDetailRes response = categoryFieldWebPort.getDetail(categoryFieldId);
        return BaseResponse.success(response);
    }

    /**
     * 특정 카테고리의 필드 목록 조회
     */
    @Operation(
            summary = "카테고리별 필드 목록 조회",
            description = "특정 카테고리에 속한 필수 필드 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
            }
    )
    @GetMapping("/category/{category}")
    public BaseResponse<CategoryFieldDto.CategoryFieldListRes> getCategoryFieldsByCategory(
            @Parameter(description = "카테고리명", required = true, example = "RESERVATION")
            @PathVariable String category) {

        log.info("카테고리별 필드 목록 조회 - category: {}", category);
        CategoryFieldDto.CategoryFieldListRes response = categoryFieldWebPort.getByCategory(category);
        return BaseResponse.success(response);
    }

    /**
     * 카테고리 필드 수정
     */
    @Operation(
            summary = "카테고리 필드 수정",
            description = "기존 카테고리 필수 필드 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드를 찾을 수 없음")
            }
    )
    @PutMapping("/{categoryFieldId}")
    public BaseResponse update(
            @Parameter(description = "카테고리 필드 ID", required = true, example = "1")
            @PathVariable Long categoryFieldId,
            @RequestBody CategoryFieldDto.CategoryFieldReq dto) {

        log.info("카테고리 필드 수정 - categoryFieldId: {}", categoryFieldId);
        categoryFieldWebPort.update(categoryFieldId, dto);
        return BaseResponse.success("필드가 수정되었습니다.");
    }

    /**
     * 카테고리 필드 삭제
     */
    @Operation(
            summary = "카테고리 필드 삭제",
            description = "카테고리 필수 필드를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "필드를 찾을 수 없음")
            }
    )
    @DeleteMapping("/{categoryFieldId}")
    public BaseResponse delete(
            @Parameter(description = "카테고리 필드 ID", required = true, example = "1")
            @PathVariable Long categoryFieldId) {

        log.info("카테고리 필드 삭제 - categoryFieldId: {}", categoryFieldId);
        categoryFieldWebPort.delete(categoryFieldId);
        return BaseResponse.success("필드가 삭제되었습니다.");
    }
}