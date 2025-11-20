package org.example.apireservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
@EnableKafka
public class ApiReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiReservationApplication.class, args);
    }

}
