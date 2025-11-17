package org.example.apiresource.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.dto.CustomFieldDto;
import org.example.apiresource.usecase.port.in.CustomFieldValueWebPort;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "커스텀 필드 값 관리", description = "커스텀 필드 값을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-field")
public class CustomFieldValueWebAdapter {
    private final CustomFieldValueWebPort customFieldValueWebPort;


    // ---------------- 필드 값 생성 --------------------
    @Operation(summary = "커스텀 필드 값 생성", description = "커스텀 필드에 대한 값을 생성합니다. 여러 값도 한 번에 저장 가능")
    @PostMapping("/values/{targetId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> registerCustomFieldValues(@PathVariable Long targetId,
                                                                                                @RequestBody List<CustomFieldDto.CustomFieldValue> dtos) {
        List<CustomFieldDto.CustomFieldValueListRes> result = customFieldValueWebPort.register(targetId, dtos);
        return BaseResponse.success(result);
    }


    // ---------------- 리소스 필드 값 조회 --------------------
    @Operation(summary = "특정 리소스의 커스텀 필드 값 조회",
            description = "특정 리소스에 대한 커스텀 필드 값을 조회합니다.")
    @GetMapping("/value/resource/{resourceId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> getResourceFieldValues(@PathVariable Long resourceId) {
        List<CustomFieldDto.CustomFieldValueListRes> response = customFieldValueWebPort.getResourceFieldValues(resourceId);
        return BaseResponse.success(response);
    }


    // -------------------- 특정 예약의 커스텀 필드 값 조회 -------------------
    @Operation(summary = "예약 기반 커스텀 필드 값 조회", description = "예약 ID에 해당하는 커스텀 필드 값을 조회합니다.")
    @GetMapping("/value/reservation/{reservationId}")
    public BaseResponse<List<CustomFieldDto.CustomFieldValueListRes>> getUserFieldValuesByReservation(@PathVariable Long reservationId) {
        List<CustomFieldDto.CustomFieldValueListRes> result = customFieldValueWebPort.getUserFieldValuesByReservation(reservationId);
        return BaseResponse.success(result);
    }


    // ---------------- RESOURCE 필드 값 수정 --------------------
    @Operation(summary = "커스텀 필드 값 수정", description = "커스텀 필드 값을 수정합니다. (RESOURCE 전용)")
    @PutMapping("/value")
    public BaseResponse updateCustomFieldValues(@RequestBody List<CustomFieldDto.CustomFieldValueUpdateReq> dtoList) {
        customFieldValueWebPort.update(dtoList);
        return BaseResponse.success("커스텀 필드 값이 수정되었습니다.");
    }


    // ---------------- 커스텀 필드 값 삭제 --------------------
    @Operation(summary = "커스텀 필드 값 삭제", description = "커스텀 필드 값을 삭제합니다.")
    @DeleteMapping("/value")
    public BaseResponse deleteCustomFieldValues(
            @RequestBody List<Long> customFieldValueIds
    ) {
        customFieldValueWebPort.delete(customFieldValueIds);
        return BaseResponse.success("커스텀 필드 값이 삭제되었습니다.");
    }
}
