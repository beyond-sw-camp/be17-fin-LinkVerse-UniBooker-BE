package org.example.apireservation.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.apireservation.domain.model.dto.AccessLogDto;
import org.example.common.base.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AccessLog", description = "리소스 접근 로그 API - 사용자의 리소스 또는 리소스 그룹 접근 이력 관리")
@RestController
@RequestMapping("/api/access-log")
public class AccessLogWebAdapter {
    @PostMapping
    @Operation(summary = "리소스 접근 로그 생성", description = "리소스 혹은 리소스 그룹 접근 시 로그 생성")
    public BaseResponse<AccessLogDto.Response> createAccessLog(@RequestBody AccessLogDto.Create request) {
        // TODO: 서비스 호출 후 생성된 DTO 반환
        return BaseResponse.success();
    }
//
//    @PutMapping
//    @Operation(summary = "리소스 접근 로그 업데이트", description = "로그 상태(CANCELLED, CONFIRMED 등) 업데이트")
//    public BaseResponse<AccessLogDto.Response> updateAccessLog(@RequestBody AccessLogDto.Update request) {
//        // TODO: 서비스 호출 후 업데이트된 DTO 반환
//        return BaseResponse.success();
//    }
//
//    @GetMapping("/resource-group/{groupId}")
//    @Operation(summary = "리소스 그룹 접근 로그 조회", description = "특정 리소스 그룹에 대한 접근 로그 조회")
//    public BaseResponse<List<AccessLogDto.Response>> getLogsByResourceGroup(@PathVariable Long groupId) {
//        // TODO: 서비스 호출 후 DTO 리스트 반환
//        List<AccessLogDto.Response> logs = List.of(); // 예시
//        return BaseResponse.success(logs);
//    }
//
//    @GetMapping("/resource/{resourceId}")
//    @Operation(summary = "리소스 접근 로그 조회", description = "특정 리소스에 대한 접근 로그 조회")
//    public BaseResponse<List<AccessLogDto.Response>> getLogsByResource(@PathVariable Long resourceId) {
//        // TODO: 서비스 호출 후 DTO 리스트 반환
//        List<AccessLogDto.Response> logs = List.of(); // 예시
//        return BaseResponse.success(logs);
//    }
}
