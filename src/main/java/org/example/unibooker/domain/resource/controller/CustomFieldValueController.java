package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.service.CustomFieldService;
import org.example.unibooker.domain.resource.service.CustomFieldValueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커스텀 필드 값 관리", description = "커스텀 필드 값을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldValueController {
    private final CustomFieldValueService customFieldValueService;


    // ---------------- 필드 값 생성 --------------------
    @Operation(summary = "커스텀 필드 값 생성", description = "커스텀 필드에 대한 값을 생성합니다. 여러 값도 한 번에 저장 가능")
    @PostMapping("/values")
    public BaseResponse registerCustomFieldValues(@RequestBody List<CustomFieldDto.CustomFieldValue> dtos) {
        customFieldValueService.register(dtos);
        return BaseResponse.success("커스텀 필드 값이 생성되었습니다.");
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
