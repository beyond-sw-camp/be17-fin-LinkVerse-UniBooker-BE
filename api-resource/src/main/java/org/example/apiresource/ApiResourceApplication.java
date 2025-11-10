package org.example.apiresource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ApiResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiResourceApplication.class, args);
    }

}
