package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.dto.SuperDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 슈퍼 관리자 서비스
 * - 슈퍼 관리자 로그인만 처리
 * - 기타 관리 기능은 AdminService 활용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperService {

    private final AuthService authService;

    /**
     * 슈퍼 관리자 로그인
     * - 이메일로만 조회, SUPER 권한만
     */
    public UserDto.LoginResponseWithToken superLogin(SuperDto.SuperLoginRequest request) {
        return authService.loginWithRole(
                request.getEmail(),
                request.getPassword(),
                UserRole.SUPER
        );
    }
}
