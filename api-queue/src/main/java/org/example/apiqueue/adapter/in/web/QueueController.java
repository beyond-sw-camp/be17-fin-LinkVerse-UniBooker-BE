package org.example.apiqueue.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.application.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService service;

    /** JOIN */
    @PostMapping("/{resourceId}/join")
    public JoinRes join(@PathVariable Long resourceId,
                        @RequestHeader("X-User-Id") Long userId) {
        var r = service.join(resourceId, userId);
        return new JoinRes(r.token(), r.position());
    }

    /** PROMOTE (앞 n명 활성화) */
    @PostMapping("/{resourceId}/promote")
    public String promote(@PathVariable Long resourceId,
                          @RequestParam(defaultValue = "1") long count) {
        service.promote(resourceId, count);
        return "OK";
    }

    /** STATUS (대기 순번/길이/ETA) */
    @GetMapping("/{resourceId}/status")
    public StatusRes status(@PathVariable Long resourceId,
                            @RequestParam String token) {
        var s = service.status(resourceId, token);
        return new StatusRes(s.position(), s.length(), s.etaSeconds());
    }

    /** CONSUME (사용 완료 처리) */
    @PostMapping("/{resourceId}/tokens/{token}/consume")
    public String consume(@PathVariable Long resourceId,
                          @PathVariable String token) {
        service.consume(resourceId, token);
        return "OK";
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "api-queue");
    }

    // DTOs
    public record JoinRes(String token, long position) {}
    public record StatusRes(long position, long length, long etaSeconds) {}
}