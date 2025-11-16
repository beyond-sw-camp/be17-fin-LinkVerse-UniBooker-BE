package org.example.apistatistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ApiStatisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiStatisticsApplication.class, args);
	}

}
