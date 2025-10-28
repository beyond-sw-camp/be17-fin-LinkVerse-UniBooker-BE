package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 슈퍼 관리자 DTO
 */
public class SuperDto {

    /**
     * 슈퍼 관리자 로그인 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "슈퍼 관리자 로그인 요청")
    public static class SuperLoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "슈퍼 관리자 이메일", example = "super@unibooker.com", required = true)
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Schema(description = "비밀번호", required = true)
        private String password;
    }

    // ========== 관리자 관리 관련 ==========

    /**
     * 특정 기업의 관리자 목록 조회 응답
     */
    @Getter
    @Builder
    @Schema(description = "기업 관리자 목록 조회 응답")
    public static class CompanyManagerListResponse {

        @Schema(description = "관리자 목록")
        private List<CompanyManagerInfo> managers;
    }

    /**
     * 기업 관리자 정보
     */
    @Getter
    @Builder
    @Schema(description = "기업 관리자 정보")
    public static class CompanyManagerInfo {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "이름", example = "김철수")
        private String name;

        @Schema(description = "이메일", example = "admin@example.com")
        private String email;

        @Schema(description = "연락처", example = "010-1234-5678")
        private String phone;

        @Schema(description = "역할", example = "ADMIN")
        private UserRole role;

        @Schema(description = "상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "생성일시", example = "2025-01-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시", example = "2025-01-15T14:30:00")
        private LocalDateTime updatedAt;
    }

    /**
     * 관리자 상태 변경 요청
     */
    @Getter
    @NoArgsConstructor
    @Schema(description = "관리자 상태 변경 요청")
    public static class ManagerStatusUpdateRequest {

        @Schema(description = "변경할 상태 (ACTIVE, SUSPENDED)", example = "ACTIVE")
        private UserStatus status;
    }

    /**
     * 관리자 상태 변경 응답
     */
    @Getter
    @Builder
    @Schema(description = "관리자 상태 변경 응답")
    public static class ManagerStatusUpdateResponse {

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "변경된 상태", example = "ACTIVE")
        private UserStatus status;

        @Schema(description = "메시지", example = "상태가 변경되었습니다.")
        private String message;

        @Schema(description = "수정일시", example = "2025-01-15T14:30:00")
        private LocalDateTime updatedAt;
    }
}
