package org.example.common.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthDto {

    // 최소 인증 정보만 포함
    private Long id; // 사용자 ID
    private Long companyId; // 소속 기업 ID
    private UserRole role;
}