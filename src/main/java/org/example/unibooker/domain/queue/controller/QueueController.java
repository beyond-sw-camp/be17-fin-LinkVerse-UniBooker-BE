package org.example.unibooker.domain.queue.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.queue.model.dto.QueueDto;
import org.example.unibooker.domain.queue.service.QueueService;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 대기열 컨트롤러
 * - 대기열 진입, 상태 조회, 입장 처리
 */
@Tag(name = "대기열", description = "예약 대기열 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    /**
     * 대기열 진입
     */
    @PostMapping("/{resourceId}/join")
    public ResponseEntity<BaseResponse<QueueDto.JoinResponse>> joinQueue(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {

        QueueDto.JoinResponse response = queueService.joinQueue(resourceId, authUser.getId());
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 대기열 상태 조회 (Polling)
     */
    @Operation(summary = "대기열 상태 조회", description = "현재 대기 순번과 입장 가능 여부를 조회합니다.")
    @GetMapping("/{resourceId}/status")
    public ResponseEntity<BaseResponse<QueueDto.StatusResponse>> getQueueStatus(
            @PathVariable Long resourceId,
            @RequestParam String token) {

        QueueDto.StatusResponse response = queueService.getQueueStatus(resourceId, token);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 토큰 소비 (입장)
     */
    @Operation(summary = "토큰 소비", description = "대기열에서 나와 예약 페이지로 입장합니다.")
    @PostMapping("/{resourceId}/tokens/{token}/consume")
    public ResponseEntity<BaseResponse<QueueDto.ConsumeResponse>> consumeToken(
            @PathVariable Long resourceId,
            @PathVariable String token) {

        QueueDto.ConsumeResponse response = queueService.consumeToken(resourceId, token);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 대기열 이탈
     */
    @Operation(summary = "대기열 이탈", description = "대기열에서 나갑니다.")
    @DeleteMapping("/{resourceId}/leave")
    public ResponseEntity<BaseResponse<String>> leaveQueue(
            @PathVariable Long resourceId,
            @RequestParam String token) {

        queueService.leaveQueue(resourceId, token);
        return ResponseEntity.ok(BaseResponse.success("대기열에서 나갔습니다."));
    }
}