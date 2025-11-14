package org.example.apiqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ApiQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiQueueApplication.class, args);
    }

}
