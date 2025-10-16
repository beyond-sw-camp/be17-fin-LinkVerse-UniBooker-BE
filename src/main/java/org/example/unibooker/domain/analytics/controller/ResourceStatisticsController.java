package org.example.unibooker.domain.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.analytics.model.ResourceStatisticsDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/resource-statistics")
@RequiredArgsConstructor
@Tag(name = "ResourceStatistics", description = "리소스 통계 관련 API")
public class ResourceStatisticsController {

    @Operation(summary = "리소스 통계 생성/업데이트")
    @PostMapping
    public BaseResponse<ResourceStatisticsDto.Response> updateStatistics( @RequestBody ResourceStatisticsDto.Request request) {
        return BaseResponse.success();
    }

    @Operation(summary = "특정 리소스 통계 조회")
    @GetMapping("/{resourceId}")
    public BaseResponse<List<ResourceStatisticsDto.Response>> getStatisticsByResource(@PathVariable Long resourceId) {
        return BaseResponse.success(List.of());
    }
}
