package org.example.unibooker.domain.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "사용자 활동 로그 DTO")
public class UserActivityDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "사용자 활동 로그 생성 요청 정보")
    public static class Create {
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "회사 ID", example = "1")
        private Long companyId;

        @Schema(description = "이벤트 유형", allowableValues = {"RESOURCE_DETAIL_VIEW","RESERVATION","CANCELLATION"})
        private String eventType;

        @Schema(description = "타겟 유형", allowableValues = {"RESOURCE","RESERVATION","RESOURCE_GROUP"})
        private String targetType;

        @Schema(description = "타겟 ID", example = "10")
        private Long targetId;

        @Schema(description = "이벤트 발생 시간")
        private LocalDateTime eventTime;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "사용자 활동 로그 응답 정보")
    public static class Response {
        @Schema(description = "로그 ID", example = "100")
        private Long id;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "회사 ID", example = "1")
        private Long companyId;

        @Schema(description = "이벤트 유형")
        private String eventType;

        @Schema(description = "타겟 유형")
        private String targetType;

        @Schema(description = "타겟 ID")
        private Long targetId;

        @Schema(description = "이벤트 발생 시간")
        private LocalDateTime eventTime;
    }
}
