package org.example.unibooker.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JdbcTemplate jdbcTemplate;

    @Value("${health.check.frontend.url:https://www.unibooker.kro.kr}")
    private String frontendUrl;

    @Value("${health.check.backend.url:https://www.unibooker.kro.kr/actuator/health}")
    private String backendUrl;

    @Autowired
    public CustomHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        Health.Builder statusBuilder = Health.up();

        // 각 컴포넌트 헬스 체크
        ServiceInfo frontendInfo = checkFrontend();
        DbInfo dbInfo = checkDatabase();
        ServiceInfo backendInfo = checkBackend();

        // 각 컴포넌트의 상세 정보 추가
        statusBuilder.withDetail("frontend", frontendInfo);
        statusBuilder.withDetail("backend", backendInfo);
        statusBuilder.withDetail("database", dbInfo);

        // 하나라도 DOWN이면 전체 상태를 DOWN으로 설정
        if (isDown(frontendInfo.status()) || isDown(backendInfo.status()) || isDown(dbInfo.status())) {
            statusBuilder.down();
        }

        return statusBuilder.build();
    }

    /**
     * 프론트엔드 서비스 상태 확인
     */
    private ServiceInfo checkFrontend() {
        // 하드코딩된 값을 상수로 관리하거나 설정으로 분리할 수 있습니다.
        return checkHttpService("Frontend", frontendUrl, "v1.0.0");
    }

    /**
     * 백엔드 서비스 상태 확인 (Actuator)
     */
    private ServiceInfo checkBackend() {
        return checkHttpService("Backend", backendUrl, "v1.0.0");
    }

    /**
     * 데이터베이스 상태 확인
     */
    private DbInfo checkDatabase() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return new DbInfo("UP", "MariaDB");
        } catch (Exception e) {
            return new DbInfo("DOWN", "MariaDB");
        }
    }

    /**
     * HTTP 기반 서비스 상태 확인을 위한 공통 메소드
     */
    private ServiceInfo checkHttpService(String name, String url, String version) {
        try {
            restTemplate.getForObject(url, String.class);
            return new ServiceInfo("UP", url, name, version);
        } catch (HttpStatusCodeException e) {
            return new ServiceInfo("DOWN (" + e.getStatusCode() + ")", url, name, version);
        } catch (Exception e) {
            return new ServiceInfo("DOWN", url, name, version);
        }
    }

    /**
     * 상태 문자열이 "DOWN"으로 시작하는지 확인하는 헬퍼 메소드
     */
    private boolean isDown(String status) {
        return status != null && status.startsWith("DOWN");
    }

    private record ServiceInfo(String status, String url, String name, String version) {}
    private record DbInfo(String status, String database) {}
}