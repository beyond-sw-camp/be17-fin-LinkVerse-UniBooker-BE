package org.example.unibooker.domain.queue.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Queue", description = "예약 대기열 API - 고객 예약 진입 및 대기 관리")
@RestController
@RequestMapping("/api/queue")
public class QueueController {
}
