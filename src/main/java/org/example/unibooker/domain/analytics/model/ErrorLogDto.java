package org.example.unibooker.domain.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "서버 에러 로그 DTO")
public class ErrorLogDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "에러 로그 생성/요청 정보")
    public static class Request {

        @Schema(description = "API 경로")
        private String apiPath;

        @Schema(description = "HTTP 메서드")
        private String method;

        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "예외 클래스명")
        private String errorType;

        @Schema(description = "에러 메시지")
        private String message;

        @Schema(description = "상태 코드")
        private Integer statusCode;

        @Schema(description = "트레이스 ID")
        private String traceId;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "에러 로그 응답 정보")
    public static class Response {

        @Schema(description = "로그 ID")
        private Long id;

        @Schema(description = "발생 시각")
        private LocalDateTime timestamp;

        @Schema(description = "로그 레벨")
        private String level;

        @Schema(description = "API 경로")
        private String apiPath;

        @Schema(description = "HTTP 메서드")
        private String method;

        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "예외 클래스명")
        private String errorType;

        @Schema(description = "에러 메시지")
        private String message;

        @Schema(description = "HTTP 상태 코드")
        private Integer statusCode;

        @Schema(description = "트레이스 ID")
        private String traceId;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "에러 로그 리스트 응답")
    public static class ListResponse {
        @Schema(description = "에러 로그 리스트")
        private List<Response> logs;
    }
}
