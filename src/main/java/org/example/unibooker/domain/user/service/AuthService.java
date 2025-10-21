package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공통 인증 서비스
 * - 모든 역할의 로그인 로직을 통합 관리
 * - 비밀번호 검증, JWT 생성 등 공통 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 일반 사용자 로그인 (USER)
     * - 이메일 + companyId로 조회
     */
    public UserDto.LoginResponse loginWithCompany(String email, String password, Long companyId) {
        // 1. 사용자 조회
        Users user = userRepository.findByEmailAndCompanyId(email, companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 검증
        validateUserStatus(user);

        // 3. 비밀번호 검증
        validatePassword(password, user.getPassword());

        // 4. 기업 승인 상태 확인
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 5. 로그인 응답 생성
        return createLoginResponse(user, company);
    }

    /**
     * 관리자/매니저 로그인 (ADMIN/MANAGER)
     * - 이메일로만 조회, ADMIN 또는 MANAGER 권한
     */
    public UserDto.LoginResponse loginWithRoles(String email, String password, List<UserRole> roles) {
        // 1. 사용자 조회
        Users user = userRepository.findByEmailAndRoleIn(email, roles)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 검증
        validateUserStatus(user);

        // 3. 비밀번호 검증
        validatePassword(password, user.getPassword());

        // 4. 기업 승인 상태 확인
        Companies company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 5. 로그인 응답 생성
        return createLoginResponse(user, company);
    }

    /**
     * 슈퍼 관리자 로그인 (SUPER)
     * - 이메일로만 조회, SUPER 권한만
     */
    public UserDto.LoginResponse loginWithRole(String email, String password, UserRole role) {
        // 1. 사용자 조회
        Users user = userRepository.findByEmailAndRoleIn(email, List.of(role))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 검증
        validateUserStatus(user);

        // 3. 비밀번호 검증
        validatePassword(password, user.getPassword());

        // 4. 로그인 응답 생성 (SUPER는 companyId 없음)
        return createLoginResponse(user, null);
    }

    /**
     * 사용자 상태 검증
     */
    private void validateUserStatus(Users user) {
        switch (user.getStatus()) {
            case DELETED:
                throw new BaseException(BaseResponseStatus.DELETED_USER);
            case INACTIVE:
                throw new BaseException(BaseResponseStatus.INACTIVE_USER);
            case SUSPENDED:
                throw new BaseException(BaseResponseStatus.SUSPENDED_USER);
            case ACTIVE:
                break;
            default:
                throw new BaseException(BaseResponseStatus.INVALID_USER_STATUS);
        }
    }

    /**
     * 비밀번호 검증
     */
    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        }
    }

    /**
     * 로그인 응답 생성
     */
    private UserDto.LoginResponse createLoginResponse(Users user, Companies company) {
        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        return UserDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .companySlug(company != null ? company.getCompanySlug() : null)
                .passwordChangeRequired(user.getIsFirstLogin())
                .build();
    }
}
