package org.example.apiqueue.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.application.service.QueueService;
import org.example.apiqueue.domain.port.out.QueueRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/queues")
@RequiredArgsConstructor
public class AdminQueueController {

    private final QueueService service;

    @GetMapping("/{resourceId}/wait")
    public List<QueueRepository.TokenView> listWait(@PathVariable Long resourceId,
                                                    @RequestParam(defaultValue="0") long offset,
                                                    @RequestParam(defaultValue="50") long limit) {
        return service.listWait(resourceId, offset, limit);
    }

    @GetMapping("/{resourceId}/active")
    public List<QueueRepository.TokenView> listActive(@PathVariable Long resourceId,
                                                      @RequestParam(defaultValue="0") long offset,
                                                      @RequestParam(defaultValue="50") long limit) {
        return service.listActive(resourceId, offset, limit);
    }

    @GetMapping("/{resourceId}/counts")
    public Map<String, Long> counts(@PathVariable Long resourceId) {
        return Map.of("wait", service.waitSize(resourceId), "active", service.activeSize(resourceId));
    }


}
