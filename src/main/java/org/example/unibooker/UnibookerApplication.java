package org.example.unibooker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UnibookerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnibookerApplication.class, args);
    }

}
