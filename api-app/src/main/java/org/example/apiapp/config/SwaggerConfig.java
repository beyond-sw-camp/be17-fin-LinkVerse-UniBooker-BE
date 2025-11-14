package org.example.apiapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger 설정
 * - SpringDoc OpenAPI 3.0 기반
 * - Swagger UI 제공
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UniBooker Main-Service API")
                        .description("""
                                UniBooker MSA 메인 서비스 API 문서
                                
                                **주요 기능:**
                                - 사용자 관리 (User, Admin, Manager)
                                - 기업 관리 (Company)
                                - 인증/인가 (JWT)
                                - 알림 (Notification)
                                - 이미지 업로드 (S3 Presigned URL)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("UniBooker Team")
                                .email("support@unibooker.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("로컬 개발 서버"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("API Gateway")
                ));
    }
}