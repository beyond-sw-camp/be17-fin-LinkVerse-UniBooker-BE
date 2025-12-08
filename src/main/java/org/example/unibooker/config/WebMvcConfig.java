package org.example.unibooker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** URL로 접근 시 실제 파일 시스템의 uploads/ 디렉토리와 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    // CORS 설정 추가
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "https://www.unibooker.kro.kr",
                        "https://www.unibooker.n-e.kr",
                        "https://unibooker.n-e.kr"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // Cookie 허용
                .maxAge(3600);
    }
}