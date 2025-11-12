package org.example.apireservation.domain.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "리소스 접근 로그 DTO")
public class AccessLogDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 접근 로그 생성 요청 정보")
    public static class Create {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "리소스 ID", example = "10")
        private Long resourceId;

        @Schema(description = "리소스 그룹 ID", example = "5")
        private Long resourceGroupId;

        @Schema(description = "접속 요청 시간")
        private LocalDateTime requestedAt;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 접근 로그 업데이트 요청 정보")
    public static class Update {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "리소스 ID", example = "10")
        private Long resourceId;

        @Schema(description = "리소스 그룹 ID", example = "5")
        private Long resourceGroupId;

        @Schema(description = "접속 시간")
        private LocalDateTime enteredAt;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "리소스 접근 로그 응답 정보")
    public static class Response {

        @Schema(description = "접근 로그 ID", example = "100")
        private Long id;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "리소스 ID", example = "10")
        private Long resourceId;

        @Schema(description = "리소스 그룹 ID", example = "5")
        private Long resourceGroupId;

        @Schema(description = "접속 요청 시간")
        private LocalDateTime requestedAt;

        @Schema(description = "접속 시간")
        private LocalDateTime enteredAt;

        @Schema(description = "취소 시간")
        private LocalDateTime cancelledAt;

        @Schema(description = "접속 상태", allowableValues = {"WAITING", "CONFIRMED", "CANCELLED"})
        private String enteredStatus;
    }
}
