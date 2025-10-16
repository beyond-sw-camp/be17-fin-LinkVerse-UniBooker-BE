package org.example.unibooker.domain.analytics.model;

import jakarta.persistence.Column;
import org.example.unibooker.common.BaseEntity;

public class ErrorLog extends BaseEntity {
    private String level; // ERROR, WARN 등

    private String apiPath;

    private String method;

    private Long userId; // 요청자, 없으면 NULL

    private String errorType; // 예외 클래스명

    @Column(length = 255)
    private String message; // 요약 메시지

    private Integer statusCode;

    private String traceId; // 요청 추적용
}
