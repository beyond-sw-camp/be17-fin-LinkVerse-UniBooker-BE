package org.example.apireservation.adapter.out.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.apireservation.usecase.port.out.ReservationKafkaPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationKafkaProducer implements ReservationKafkaPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "reservation-completed";

    @Override
    public void publishReservationCompleted(Long userId, String resourceName) {
        try {
            Map<String, Object> event = Map.of(
                    "userId", userId,
                    "resourceName", resourceName
            );
            kafkaTemplate.send(TOPIC, event);
            log.info("📤 Kafka 메시지 발송 완료 - topic: {}, userId: {}, resourceName: {}", TOPIC, userId, resourceName);
        } catch (Exception e) {
            log.error("❌ Kafka 메시지 발송 실패 - userId: {}, resourceName: {}", userId, resourceName, e);
        }
    }
}
