package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.CustomTargetType;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.usecase.port.in.CustomFieldWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커스텀 필드 관리", description = "커스텀 필드 정의를 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldWebAdapter {
    private final CustomFieldWebPort customFieldWebPort;


    // ---------------- 필드 생성 ----------------
    @Operation(summary = "커스텀 필드 생성", description = "서비스 또는 사용자 커스텀 필드를 생성합니다.")
    @PostMapping("/{resourceGroupId}")
    public BaseResponse registerCustomField(
            @PathVariable Long resourceGroupId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        customFieldWebPort.create(resourceGroupId, dto);
        return BaseResponse.success("커스텀 필드가 생성되었습니다.");
    }


    // ---------------- 필드 조회 ----------------
    @Operation(summary = "커스텀 필드 조회", description = "서비스 그룹의 커스텀 필드 목록을 조회합니다.")
    @GetMapping("/{resourceGroupId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldRes>> getCustomFields(
            @PathVariable Long resourceGroupId,
            @RequestParam(required = false) CustomTargetType type // SERVICE / USER / null(전체)
    ) {
        List<CustomFieldDto.CustomFieldRes> fields = customFieldWebPort.getCustomFields(resourceGroupId, type);
        return BaseResponse.success(fields);
    }


    // ---------------- 필드 수정 ----------------
    @Operation(summary = "커스텀 필드 수정", description = "기존 커스텀 필드를 수정합니다.")
    @PutMapping("/{customFieldId}")
    public BaseResponse updateCustomField(
            @PathVariable Long customFieldId,
            @RequestBody CustomFieldDto.CustomFieldReq dto
    ) {
        customFieldWebPort.update(customFieldId, dto);
        return BaseResponse.success("커스텀 필드가 수정되었습니다.");
    }


    // ---------------- 필드 삭제 ----------------
    @Operation(summary = "커스텀 필드 삭제", description = "커스텀 필드를 삭제합니다.")
    @DeleteMapping("/{customFieldId}")
    public BaseResponse deleteCustomField(@PathVariable Long customFieldId) {
        customFieldWebPort.delete(customFieldId);
        return BaseResponse.success("커스텀 필드가 삭제되었습니다.");
    }
}