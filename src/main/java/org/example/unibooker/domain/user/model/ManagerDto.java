package org.example.unibooker.domain.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ManagerDto {

    /**
     * 매니저 계정 생성 요청 DTO
     */
    @Getter
    public static class CreateRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "이름은 필수입니다")
        private String name;

        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
        private String phone;
    }

    /**
     * 매니저 계정 생성 응답 DTO
     */
    @Getter
    @Builder
    public static class CreateResponse {
        private String message;
        private Long managerId;
        private String email;
        private String name;
        private String companyName;
        private LocalDateTime createdAt;
    }
}