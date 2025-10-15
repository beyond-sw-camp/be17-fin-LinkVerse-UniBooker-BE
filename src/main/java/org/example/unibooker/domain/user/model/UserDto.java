package org.example.unibooker.domain.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    // ========== 일반 사용자 회원가입 Request ==========

    @Getter
    @NoArgsConstructor
    public static class SignUpRequest {

        @NotBlank(message = "이름은 필수입니다")
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
        private String name;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        private String password;

        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
        private String phone;
    }

    // ========== 회원가입 Response ==========

    @Getter
    @Builder
    public static class SignUpResponse {
        private Long id;
        private String name;
        private String email;
        private UserRole role;
        private UserStatus status;
        private LocalDateTime createdAt;
    }

    // ========== 비밀번호 변경 Request ==========

    @Getter
    @NoArgsConstructor
    public static class PasswordChangeRequest {

        @NotBlank(message = "현재 비밀번호는 필수입니다")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다")
        private String newPassword;

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        private String confirmPassword;
    }

    // ========== 로그인 Response (향후 사용) ==========

    @Getter
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String name;
        private String email;
        private UserRole role;
        private Boolean passwordChangeRequired;
        private Long companyId;
    }
}