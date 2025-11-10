package org.example.apiqueue.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.adapter.out.persistence.RedisQueueRepository;
import org.example.apiqueue.application.service.QueueService;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService service;
    private final QueueRepository repo; // status 계산용

    /** JOIN */
    @PostMapping("/{serviceGroupId}/join")
    public JoinRes join(@PathVariable Long serviceGroupId,
                        @RequestHeader("X-User-Id") Long userId) {
        var r = service.join(serviceGroupId, userId);
        return new JoinRes(r.token(), r.position());
    }

    /** PROMOTE (앞 n명 활성화) */
    @PostMapping("/{serviceGroupId}/promote")
    public String promote(@PathVariable Long serviceGroupId,
                          @RequestParam(defaultValue = "1") long count) {
        service.promote(serviceGroupId, count);
        return "OK";
    }

    /** STATUS (대기 순번/길이/ETA) */
    @GetMapping("/{serviceGroupId}/status")
    public StatusRes status(@PathVariable Long serviceGroupId,
                            @RequestParam String token) {
        var s = service.status(serviceGroupId, token);
        return new StatusRes(s.position(), s.length(), s.etaSeconds());
    }

    /** CONSUME (사용 완료 처리) */
    @PostMapping("/{serviceGroupId}/tokens/{token}/consume")
    public String consume(@PathVariable Long serviceGroupId,
                          @PathVariable String token) {
        service.consume(serviceGroupId, token);
        return "OK";
    }

    // DTOs
    public record JoinRes(String token, long position) {}
    public record StatusRes(long position, long length, long etaSeconds) {}
}