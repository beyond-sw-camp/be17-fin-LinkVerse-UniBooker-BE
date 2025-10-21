package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.model.CustomTargetType;
import org.example.unibooker.domain.resource.service.CustomFieldService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커스텀 필드 관리", description = "커스텀 필드 정의와 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldController {
    private final CustomFieldService customFieldService;

    // ---------------- 필드 정의 --------------------

    // ---------------- 필드 생성 ----------------
    @Operation(summary = "커스텀 필드 생성", description = "서비스 또는 사용자 커스텀 필드를 생성합니다.")
    @PostMapping("/{resourceGroupId}")
    public BaseResponse registerCustomField(
            @PathVariable Long resourceGroupId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        customFieldService.create(resourceGroupId, dto);
        return BaseResponse.success("커스텀 필드가 생성되었습니다.");
    }


    // ---------------- 필드 조회 ----------------
    @Operation(summary = "커스텀 필드 조회", description = "서비스 그룹의 커스텀 필드 목록을 조회합니다.")
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldRes>> getCustomFields(
            @PathVariable Long resourceGroupId,
            @RequestParam(required = false) CustomTargetType type // SERVICE / USER / null(전체)
    ) {
        List<CustomFieldDto.CustomFieldRes> fields = customFieldService.getCustomFields(resourceGroupId, type);
        return BaseResponse.success(fields);
    }


    // ---------------- 필드 수정 ----------------
    @Operation(summary = "커스텀 필드 수정", description = "기존 커스텀 필드를 수정합니다.")
    @PutMapping("/{customFieldId}")
    public BaseResponse updateCustomField(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        customFieldService.update(customFieldId, dto);
        return BaseResponse.success("커스텀 필드가 수정되었습니다.");
    }


    // ---------------- 필드 삭제 ----------------
    @Operation(summary = "커스텀 필드 삭제", description = "커스텀 필드를 삭제합니다.")
    @DeleteMapping("/{customFieldId}")
    public BaseResponse deleteCustomField(@PathVariable Long customFieldId) {
        customFieldService.delete(customFieldId);
        return BaseResponse.success("커스텀 필드가 삭제되었습니다.");
    }




    // ---------------- 필드 값 --------------------

    // ---------------- 필드 값 생성 --------------------
    @Operation(summary = "커스텀 필드 값 생성", description = "커스텀 필드에 대한 값을 생성합니다.")
    @PostMapping("/value/{customFieldId}")
    public void registerCustomFieldValue(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldValue dto
    ) {
        // TODO: dto.getFieldType() 보고 ResourceCustomFieldValues 또는 UserCustomFieldValues 저장
    }

    @Operation(summary = "커스텀 필드 값 조회", description = "커스텀 필드에 대한 값을 조회합니다.")
    @GetMapping("/value/{customFieldValueId}")
    public CustomFieldDto.CustomFieldValueListRes getCustomFieldValue(@PathVariable Long customFieldValueId) {
        // TODO
        return null;
    }

    @Operation(summary = "커스텀 필드 값 수정", description = "커스텀 필드 값을 수정합니다.")
    @PutMapping("/value/{customFieldValueId}")
    public void updateCustomFieldValue(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldValue dto
    ) {
        // TODO
    }
}
