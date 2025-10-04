package org.example.unibooker.domain.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.unibooker.domain.company.model.CompanyStatus;

import java.time.LocalDateTime;

public class AdminDto {

    // ========== 관리자 회원가입 신청 Request ==========

    @Getter
    @NoArgsConstructor
    public static class SignUpRequest {

        @NotBlank(message = "기업명은 필수입니다")
        @Size(min = 2, max = 100, message = "기업명은 2~100자여야 합니다")
        private String companyName;

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "연락처는 필수입니다")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
        private String phone;
    }

    // ========== 관리자 회원가입 신청 Response ==========

    @Getter
    @Builder
    public static class SignUpResponse {
        private String message;
        private String email;
        private String companyName;
        private Integer estimatedDays;
    }

    // ========== 승인 상태 조회 Response ==========

    @Getter
    @Builder
    public static class StatusResponse {
        private CompanyStatus status;
        private String companyName;
        private String email;
        private String rejectionReason;
        private LocalDateTime appliedAt;
    }
}