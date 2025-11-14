package org.example.apiqueue.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.example.apiqueue.application.service.QueueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class AllowedController {

    private final QueueService service;

    @GetMapping("/allowed")
    public AllowedRes allowed(@RequestParam("queue") Long resourceId,
                              @RequestParam("token") String token) {
        boolean allowed = service.isAllowed(resourceId, token);
        return new AllowedRes(allowed);
    }

    public record AllowedRes(boolean allowed) {}
}