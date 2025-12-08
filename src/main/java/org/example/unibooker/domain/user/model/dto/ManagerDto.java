package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.user.model.UserStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ManagerDto {

    // ========== 매니저 계정 생성 요청 ==========

    @Getter
    @NoArgsConstructor
    @Schema(description = "매니저 계정 생성 요청 (ADMIN만 가능)")
    public static class CreateRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        @Schema(description = "매니저 이메일 (users.email)", example = "manager@abc.com", required = true)
        private String email;

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        @Schema(description = "매니저 이름 (users.name)", example = "박매니저", required = true)
        private String name;

        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
        @Schema(description = "매니저 연락처 (users.phone) - 010-XXXX-XXXX 형식",
                example = "010-5678-1234")
        private String phone;
    }

    // ========== 매니저 계정 생성 응답 ==========

    @Getter
    @Builder
    @Schema(description = "매니저 계정 생성 응답")
    public static class CreateResponse {

        @Schema(description = "처리 결과 메시지",
                example = "매니저 계정이 생성되었습니다. 임시 비밀번호가 이메일로 전송되었습니다.")
        private String message;

        @Schema(description = "매니저 사용자 ID (users.user_id)", example = "5")
        private Long managerId;

        @Schema(description = "매니저 이메일 (users.email)", example = "manager@abc.com")
        private String email;

        @Schema(description = "매니저 이름 (users.name)", example = "박매니저")
        private String name;

        @Schema(description = "소속 기업명 (companies.name)", example = "ABC 회사")
        private String companyName;

        @Schema(description = "계정 생성 일시 (users.created_at)", example = "2025-10-16T14:30:00")
        private LocalDateTime createdAt;
    }

    // ========== 매니저 목록 조회 Response ==========

    /**
     * 관리자가 매니저 목록 조회 응답 DTO (페이징)
     */
    @Getter
    @Builder
    @Schema(description = "매니저 목록 조회 응답 (ADMIN 전용)")
    public static class ManagerListResponse {

        @Schema(description = "매니저 목록")
        private List<ManagerInfo> managers;

        @Schema(description = "전체 항목 수", example = "15")
        private Long totalElements;

        @Schema(description = "전체 페이지 수", example = "2")
        private Integer totalPages;

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private Integer currentPage;

        @Schema(description = "페이지당 항목 수", example = "10")
        private Integer pageSize;

        /**
         * 매니저 개별 정보
         */
        @Getter
        @Builder
        @Schema(description = "매니저 정보")
        public static class ManagerInfo {

            @Schema(description = "매니저 ID (users.user_id)", example = "5")
            private Long managerId;

            @Schema(description = "매니저 이름 (users.name)", example = "박매니저")
            private String name;

            @Schema(description = "매니저 이메일 (users.email)", example = "manager@abc.com")
            private String email;

            @Schema(description = "연락처 (users.phone)", example = "010-5678-1234")
            private String phone;

            @Schema(description = "계정 상태 (users.status)", example = "ACTIVE")
            private UserStatus status;

            @Schema(description = "첫 로그인 여부 (users.is_first_login)", example = "false")
            private Boolean isFirstLogin;

            @Schema(description = "계정 생성 일시 (users.created_at)", example = "2025-10-16T14:30:00")
            private LocalDateTime createdAt;

            @Schema(description = "마지막 로그인 일시 (users.last_login_at)", example = "2025-10-16T15:00:00")
            private LocalDateTime lastLoginAt;
        }
    }

    // ========== 매니저 삭제 Response ==========

    /**
     * 관리자가 매니저 삭제 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "매니저 삭제 응답")
    public static class ManagerDeleteResponse {

        @Schema(description = "처리 결과 메시지",
                example = "매니저 계정이 삭제되었습니다.")
        private String message;

        @Schema(description = "삭제된 매니저 ID (users.user_id)", example = "5")
        private Long managerId;

        @Schema(description = "삭제된 매니저 이름 (users.name)", example = "박매니저")
        private String name;

        @Schema(description = "삭제된 매니저 이메일 (users.email)", example = "manager@abc.com")
        private String email;

        @Schema(description = "삭제 일시", example = "2025-10-16T15:30:00")
        private LocalDateTime deletedAt;
    }

    // ========== 매니저 수정 요청 DTO ==========

    /**
     * 매니저 정보 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {

        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 50, message = "이름은 50자 이내로 입력해주세요")
        private String name;

        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "연락처는 010-1234-5678 형식이어야 합니다")
        private String phone;
    }

    /**
     * 매니저 정보 수정 응답 DTO
     */
    @Getter
    @Builder
    @Schema(description = "매니저 수정 응답")
    public static class UpdateResponse {

        @Schema(description = "처리 결과 메시지", example = "매니저 정보가 수정되었습니다.")
        private String message;

        @Schema(description = "매니저 ID", example = "5")
        private Long managerId;

        @Schema(description = "매니저 이름", example = "박매니저")
        private String name;

        @Schema(description = "매니저 이메일", example = "manager@abc.com")
        private String email;

        @Schema(description = "매니저 연락처", example = "010-5678-1234")
        private String phone;

        @Schema(description = "수정 일시", example = "2025-10-16T15:30:00")
        private LocalDateTime updatedAt;
    }
}