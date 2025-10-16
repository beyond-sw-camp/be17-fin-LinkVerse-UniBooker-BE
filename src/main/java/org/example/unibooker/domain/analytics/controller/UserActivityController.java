package org.example.unibooker.domain.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.analytics.model.UserActivityDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/user-activity")
@RequiredArgsConstructor
@Tag(name = "UserActivity", description = "사용자 활동 로그 관련 API")
public class UserActivityController {

    @Operation(summary = "사용자 활동 로그 생성")
    @PostMapping
    public BaseResponse<UserActivityDto.Response> createUserActivity( @RequestBody UserActivityDto.Create request) {
        return BaseResponse.success();
    }

    @Operation(summary = "특정 사용자 활동 로그 조회")
    @GetMapping("/{userId}")
    public BaseResponse<List<UserActivityDto.Response>> getUserActivities(@PathVariable Long userId) {
        return BaseResponse.success(List.of());
    }
}
