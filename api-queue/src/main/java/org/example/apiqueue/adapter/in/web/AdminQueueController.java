package org.example.apiqueue.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apiqueue.application.service.QueueService;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 관리자 대기열 관리 컨트롤러
 * - 대기열 및 활성 토큰 목록 조회
 * - 대기열 통계 조회
 * - 관리자 권한 필요
 */
@Slf4j
@Tag(name = "Admin Queue API", description = "관리자 대기열 관리 API")
@RestController
@RequestMapping("/api/admin/queues")
@RequiredArgsConstructor
public class AdminQueueController {

    /** 대기열 서비스 */
    private final QueueService service;

    /**
     * 대기 중인 토큰 목록 조회
     */
    @Operation(
            summary = "대기 중인 토큰 목록 조회",
            description = "특정 리소스의 대기 중인 토큰 목록을 페이징하여 조회합니다. (관리자 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/{resourceId}/wait")
    public List<QueueRepository.TokenView> listWait(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "시작 위치 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") long offset,

            @Parameter(description = "조회할 개수", example = "50")
            @RequestParam(defaultValue = "50") long limit) {

        log.info("대기 중인 토큰 목록 조회 - resourceId: {}, offset: {}, limit: {}", resourceId, offset, limit);
        return service.listWait(resourceId, offset, limit);
    }

    /**
     * 활성화된 토큰 목록 조회
     */
    @Operation(
            summary = "활성화된 토큰 목록 조회",
            description = "특정 리소스의 활성화된 토큰 목록을 페이징하여 조회합니다. (관리자 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/{resourceId}/active")
    public List<QueueRepository.TokenView> listActive(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "시작 위치 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") long offset,

            @Parameter(description = "조회할 개수", example = "50")
            @RequestParam(defaultValue = "50") long limit) {

        log.info("활성화된 토큰 목록 조회 - resourceId: {}, offset: {}, limit: {}", resourceId, offset, limit);
        return service.listActive(resourceId, offset, limit);
    }

    /**
     * 대기열 통계 조회
     */
    @Operation(
            summary = "대기열 통계 조회",
            description = "특정 리소스의 대기 중인 토큰 수와 활성화된 토큰 수를 조회합니다. (관리자 권한 필요)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/{resourceId}/counts")
    public Map<String, Long> counts(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId) {

        log.info("대기열 통계 조회 - resourceId: {}", resourceId);
        long waitCount = service.waitSize(resourceId);
        long activeCount = service.activeSize(resourceId);

        return Map.of(
                "wait", waitCount,
                "active", activeCount
        );
    }
}