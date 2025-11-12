package org.example.apidiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class ApiDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiDiscoveryApplication.class, args);
    }

}
