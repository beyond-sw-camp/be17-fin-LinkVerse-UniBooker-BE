package org.example.common.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.common.model.UserRole;

@Getter
@Setter // 테스트용..지우세요!!
@Builder
public class AuthDto {

    // 최소 인증 정보만 포함
    private Long id; // 사용자 ID
    private Long companyId; // 소속 기업 ID
    private UserRole role;
}