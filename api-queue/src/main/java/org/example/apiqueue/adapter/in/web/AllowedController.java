package org.example.apiqueue.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiqueue.application.service.QueueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대기열 허용 여부 확인 컨트롤러
 * - 토큰 활성화 상태 확인
 * - 예약 진행 가능 여부 검증
 */
@Slf4j
@Tag(name = "Queue Validation API", description = "대기열 허용 여부 확인 API")
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class AllowedController {

    /** 대기열 서비스 */
    private final QueueService service;

    /**
     * 토큰 활성화 여부 확인
     */
    @Operation(
            summary = "토큰 활성화 여부 확인",
            description = "해당 토큰이 활성화되어 예약을 진행할 수 있는지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    @GetMapping("/allowed")
    public AllowedRes allowed(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @RequestParam("queue") Long resourceId,

            @Parameter(description = "대기열 토큰", required = true, example = "abc123def456")
            @RequestParam("token") String token) {

        log.info("토큰 활성화 여부 확인 - resourceId: {}, token: {}", resourceId, token);
        boolean allowed = service.isAllowed(resourceId, token);
        return new AllowedRes(allowed);
    }

    /**
     * 토큰 활성화 여부 응답 DTO
     */
    @Schema(description = "토큰 활성화 여부 응답")
    public record AllowedRes(
            @Schema(description = "활성화 여부 (true: 예약 가능, false: 대기 중)", example = "true")
            boolean allowed
    ) {}
}