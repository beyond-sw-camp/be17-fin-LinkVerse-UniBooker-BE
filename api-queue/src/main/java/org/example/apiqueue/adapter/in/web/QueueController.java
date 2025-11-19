package org.example.apiqueue.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiqueue.application.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 대기열 관리 컨트롤러
 * - 대기열 참여 및 상태 조회
 * - 토큰 활성화 및 사용 완료 처리
 * - 대기열 순번 및 예상 대기 시간 조회
 */
@Slf4j
@Tag(name = "Queue API", description = "대기열 관리 API")
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueueController {

    /** 대기열 서비스 */
    private final QueueService service;

    /**
     * 대기열 참여
     */
    @Operation(
            summary = "대기열 참여",
            description = "특정 리소스의 대기열에 참여하고 토큰과 현재 순번을 받습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대기열 참여 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @PostMapping("/{resourceId}/join")
    public JoinRes join(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("대기열 참여 - resourceId: {}, userId: {}", resourceId, userId);
        var r = service.join(resourceId, userId);
        return new JoinRes(r.token(), r.position());
    }

    /**
     * 대기열 토큰 활성화 (앞 n명 승격)
     */
    @Operation(
            summary = "대기열 토큰 활성화",
            description = "대기 중인 사용자 중 앞 n명을 활성화하여 예약 가능 상태로 전환합니다. (관리자 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "활성화 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping("/{resourceId}/promote")
    public String promote(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "활성화할 인원 수", example = "1")
            @RequestParam(defaultValue = "1") long count) {

        log.info("대기열 토큰 활성화 - resourceId: {}, count: {}", resourceId, count);
        service.promote(resourceId, count);
        return "OK";
    }

    /**
     * 대기열 상태 조회
     */
    @Operation(
            summary = "대기열 상태 조회",
            description = "현재 대기 순번, 전체 대기 인원, 예상 대기 시간을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
                    @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
            }
    )
    @GetMapping("/{resourceId}/status")
    public StatusRes status(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "대기열 토큰", required = true, example = "abc123def456")
            @RequestParam String token) {

        log.info("대기열 상태 조회 - resourceId: {}, token: {}", resourceId, token);
        var s = service.status(resourceId, token);
        return new StatusRes(s.position(), s.length(), s.etaSeconds());
    }

    /**
     * 토큰 사용 완료 처리
     */
    @Operation(
            summary = "토큰 사용 완료 처리",
            description = "예약이 완료되면 토큰을 소비 처리하여 대기열에서 제거합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "처리 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
                    @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
            }
    )
    @PostMapping("/{resourceId}/tokens/{token}/consume")
    public String consume(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "대기열 토큰", required = true, example = "abc123def456")
            @PathVariable String token) {

        log.info("토큰 사용 완료 처리 - resourceId: {}, token: {}", resourceId, token);
        service.consume(resourceId, token);
        return "OK";
    }

    /**
     * 헬스 체크
     */
    @Operation(
            summary = "헬스 체크",
            description = "api-queue 서비스의 정상 작동 여부를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "서비스 정상")
            }
    )
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "api-queue"
        );
    }

    // ========== DTO ==========

    /**
     * 대기열 참여 응답 DTO
     */
    @Schema(description = "대기열 참여 응답")
    public record JoinRes(
            @Schema(description = "대기열 토큰", example = "abc123def456")
            String token,

            @Schema(description = "현재 대기 순번 (0: 즉시 활성화)", example = "5")
            long position
    ) {}

    /**
     * 대기열 상태 응답 DTO
     */
    @Schema(description = "대기열 상태 응답")
    public record StatusRes(
            @Schema(description = "현재 대기 순번", example = "3")
            long position,

            @Schema(description = "전체 대기 인원", example = "10")
            long length,

            @Schema(description = "예상 대기 시간 (초)", example = "180")
            long etaSeconds
    ) {}
}