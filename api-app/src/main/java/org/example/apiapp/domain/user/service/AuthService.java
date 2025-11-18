package org.example.apiapp.domain.user.service;

import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.util.JwtUtil;
import org.example.apiapp.domain.company.model.entity.Companies;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.company.repository.CompanyRepository;
import org.example.apiapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.common.model.UserRole;

import java.util.Optional;

/**
 * 공통 인증 서비스
 * - 권한별 로그인 (loginWithRoles, loginWithCompany)
 * - 토큰 갱신
 * - 로그아웃
 *
 * [변경 이력]
 * - 레거시 login, signUp, adminSignUp 메서드 제거
 * - 권한별 로그인 메서드만 유지 (loginWithRoles, loginWithCompany)
 * - 토큰 갱신은 refreshTokenWithRole만 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 토큰 갱신 (쿠키 방식용 - 권한 정보 포함 반환)
     * - AuthController의 /api/auth/refresh에서 사용
     */
    public org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken refreshTokenWithRole(String refreshToken) {
        log.info("토큰 갱신 (쿠키 방식)");

        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 사용자 ID 추출
        Long userId = jwtUtil.getUserId(refreshToken);

        // 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId(),
                user.getName()
        );
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        return org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .companySlug(null)  // 토큰 갱신 시 불필요
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    /**
     * 역할 제한 로그인 (ADMIN, MANAGER, SUPER용)
     * - AdminService, SuperService에서 사용
     */
    public org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken loginWithRoles(
            String email,
            String password,
            java.util.List<UserRole> allowedRoles) {

        log.info("역할 제한 로그인 시도 - email: {}, allowedRoles: {}", email, allowedRoles);

        // 역할 중 하나로 사용자 조회 시도
        Users user = null;
        for (UserRole role : allowedRoles) {
            Optional<Users> foundUser = userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role);
            if (foundUser.isPresent()) {
                user = foundUser.get();
                break;
            }
        }

        // 사용자를 찾지 못한 경우 (보안)
        if (user == null) {
            throw new BaseException(BaseResponseStatus.INVALID_CREDENTIALS);  // ✅ 통합 메시지
        }

        // 비밀번호 확인 (보안)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException(BaseResponseStatus.INVALID_CREDENTIALS);  // ✅ 통합 메시지
        }

        // 계정 상태 확인 (DELETED, SUSPENDED, INACTIVE 순서로 체크)
        if (user.isDeleted()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_DELETED);
        }

        if (user.isSuspended()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_SUSPENDED);
        }

        if (user.isInactive()) {
            throw new BaseException(BaseResponseStatus.INACTIVE_USER);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId(),
                user.getName()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        // Company 정보 조회
        String companySlug = null;
        if (user.getCompanyId() != null) {
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(user.getCompanyId())
                    .orElse(null);
            if (company != null) {
                companySlug = company.getCompanySlug();
            }
        }

        return org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .companySlug(companySlug)
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    /**
     * 일반 사용자 로그인 (이메일 + 기업 ID)
     * - UserService에서 사용
     * - USER 역할만 허용
     */
    public org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken loginWithCompany(
            String email,
            String password,
            Long companyId) {

        log.info("일반 사용자 로그인 시도 - email: {}, companyId: {}", email, companyId);

        // 1. 사용자 조회 (USER role + 기업 ID로 조회)
        Users user = userRepository.findByEmailAndCompanyIdAndRoleAndDeletedAtIsNull(
                        email,
                        companyId,
                        UserRole.USER)
                .orElseThrow(() -> new org.example.common.exception.BaseException(
                        org.example.common.base.BaseResponseStatus.USER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new org.example.common.exception.BaseException(
                    org.example.common.base.BaseResponseStatus.INVALID_PASSWORD);
        }

        // 3. 계정 상태 확인
        if (!user.isActive()) {
            if (user.isSuspended()) {
                throw new org.example.common.exception.BaseException(
                        org.example.common.base.BaseResponseStatus.ACCOUNT_SUSPENDED);
            }
            throw new org.example.common.exception.BaseException(
                    org.example.common.base.BaseResponseStatus.INACTIVE_USER);
        }

        // 4. 기업 조회 (companySlug 제공용)
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new org.example.common.exception.BaseException(
                        org.example.common.base.BaseResponseStatus.COMPANY_NOT_FOUND));

        // 5. JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId(),
                user.getName()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        // 6. DTO 반환
        return org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .companySlug(company.getCompanySlug())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    /**
     * 로그아웃
     * - 현재는 Stateless JWT 사용으로 로그만 기록
     * - 추후 Redis 기반 Refresh Token 저장소 구현 시 실제 토큰 삭제
     */
    @Transactional
    public org.example.apiapp.domain.user.model.dto.AuthDto.LogoutResponse logout(Long userId) {
        log.info("로그아웃 - userId: {}", userId);

        // TODO: Redis 기반 Refresh Token 삭제
        // tokenStorageService.deleteRefreshToken(userId);

        return org.example.apiapp.domain.user.model.dto.AuthDto.LogoutResponse.builder()
                .message("로그아웃되었습니다.")
                .build();
    }

    /**
     * 모든 토큰 무효화 (비밀번호 변경 시)
     * - 현재는 Stateless JWT 사용으로 실제 무효화 없음
     * - 향후 Redis 기반 토큰 블랙리스트 구현 예정
     */
    public void invalidateAllTokens(Long userId) {
        log.info("모든 토큰 무효화 요청 - userId: {}", userId);
        // TODO: Redis 기반 토큰 블랙리스트 구현
        // 현재는 로그만 남김
    }
}