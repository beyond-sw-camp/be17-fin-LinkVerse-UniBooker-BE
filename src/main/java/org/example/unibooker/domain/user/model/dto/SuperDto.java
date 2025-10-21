package org.example.unibooker.domain.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
