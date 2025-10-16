package org.example.unibooker.domain.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.analytics.model.ErrorLogDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Error Log", description = "서버 에러 로그 API - 플랫폼 관리자용")
@RestController
@RequestMapping("/api/analytics/error-log")
public class ErrorLogController {

    @PostMapping
    @Operation(summary = "에러 로그 생성", description = "서버 에러 발생 시 요약 로그 생성")
    public BaseResponse<ErrorLogDto.Response> createErrorLog(@RequestBody ErrorLogDto.Request request) {
        // TODO: 서비스 호출 후 생성된 DTO 반환
        return BaseResponse.success();
    }

    @GetMapping
    @Operation(summary = "에러 로그 조회", description = "전체 에러 로그 또는 필터링 조건별 조회")
    public BaseResponse<ErrorLogDto.ListResponse> getErrorLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String apiPath
    ) {
        // TODO: 서비스 호출 후 DTO 리스트 반환
        List<ErrorLogDto.Response> logs = List.of(); // 예시
        return BaseResponse.success(new ErrorLogDto.ListResponse(logs));
    }
}
