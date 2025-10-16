package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.resource.model.CustomFieldDto;
import org.example.unibooker.domain.resource.model.CustomeTargetType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커스텀 필드 관리", description = "커스텀 필드 정의와 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldController {

    // ---------------- 필드 정의 --------------------

    // ---------------- 필드 생성 ----------------
    @Operation(summary = "커스텀 필드 생성", description = "서비스 또는 사용자 커스텀 필드를 생성합니다.")
    @PostMapping("/{resourceGroupId}")
    public void registerCustomField(
            @PathVariable Long resourceGroupId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        // TODO: 서비스 레이어에서 CustomFieldType 보고 저장
    }


    // ---------------- 필드 조회 ----------------
    @Operation(summary = "커스텀 필드 조회", description = "서비스 그룹의 커스텀 필드 목록을 조회합니다.")
    @GetMapping("/{resourceGroupId}")
    public List<CustomFieldDto.CustomFieldRes> getCustomFields(
            @PathVariable Long resourceGroupId,
            @RequestParam(required = false) CustomeTargetType type // SERVICE / USER
    ) {
        // TODO: 서비스 레이어에서 type 필터링
        return List.of();
    }


    // ---------------- 필드 수정 ----------------
    @Operation(summary = "커스텀 필드 수정", description = "기존 커스텀 필드를 수정합니다.")
    @PutMapping("/{customFieldId}")
    public void updateCustomField(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        // TODO
    }


    // ---------------- 필드 삭제 ----------------
    @Operation(summary = "커스텀 필드 삭제", description = "커스텀 필드를 삭제합니다.")
    @DeleteMapping("/{customFieldId}")
    public void deleteCustomField(@PathVariable Long customFieldId) {
        // TODO
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
    @GetMapping("/value/{customFieldId}")
    public CustomFieldDto.CustomFieldValueListRes getCustomFieldValue(@PathVariable Long customFieldId) {
        // TODO
        return null;
    }

    @Operation(summary = "커스텀 필드 값 수정", description = "커스텀 필드 값을 수정합니다.")
    @PutMapping("/value/{customFieldId}")
    public void updateCustomFieldValue(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldValue dto
    ) {
        // TODO
    }
}
