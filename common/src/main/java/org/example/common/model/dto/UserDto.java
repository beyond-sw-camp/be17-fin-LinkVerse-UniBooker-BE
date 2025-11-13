package org.example.common.model.dto;

import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 DTO (Common 라이브러리)
 * - 서비스 간 통신용 최소 DTO만 포함
 */
public class UserDto {

    /**
     * 서비스 간 통신용 기본 사용자 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInfo {

        /** 사용자 ID */
        private Long userId;

        /** 이메일 */
        private String email;

        /** 이름 */
        private String name;

        /** 역할 */
        private UserRole role;

        /** 소속 기업 ID */
        private Long companyId;

        /** 계정 상태 */
        private UserStatus status;
    }
}