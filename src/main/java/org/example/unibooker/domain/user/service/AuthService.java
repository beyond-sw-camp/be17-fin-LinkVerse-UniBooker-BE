package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.common.exception.RefreshTokenException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공통 인증 서비스
 * - 모든 역할의 로그인 로직을 통합 관리
 * - 비밀번호 검증, JWT 생성 등 공통 기능 제공
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
    private final TokenStorageService tokenStorageService;

    /**
     * 일반 사용자 로그인 (USER)
     * - 이메일 + companyId + role(USER)로 조회
     */
    public UserDto.LoginResponseWithToken loginWithCompany(String email, String password, Long companyId) {
        // 1. 사용자 조회 (USER role 명시, DELETED 제외)
        Users user = userRepository.findByEmailAndCompanyIdAndRoleAndStatusNot(
                        email,
                        companyId,
                        UserRole.USER,  // ← role 명시 추가
                        UserStatus.DELETED
                )
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 검증
        validateUserStatus(user);

        // 3. 비밀번호 검증
        validatePassword(password, user.getPassword());

        // 4. 기업 승인 상태 확인
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 5. 로그인 응답 생성
        return createLoginResponse(user, company);
    }

    /**
     * 관리자/매니저 로그인 (ADMIN/MANAGER)
     * - 이메일로만 조회, ADMIN 또는 MANAGER 권한
     */
    public UserDto.LoginResponseWithToken loginWithRoles(String email, String password, List<UserRole> roles) {
        // 1. 사용자 조회 (DELETED 제외)
        Users user = userRepository.findByEmailAndRoleInAndStatusNot(email, roles, UserStatus.DELETED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 검증
        validateUserStatus(user);

        // 3. 비밀번호 검증
        validatePassword(password, user.getPassword());

        // 4. 기업 승인 상태 확인
        Companies company = companyRepository.findById(user.getCompany().getId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 5. 로그인 응답 생성
        return createLoginResponse(user, company);
    }

    /**
     * 슈퍼 관리자 로그인 (SUPER)
     * - 이메일로만 조회, SUPER 권한만
     */
    public UserDto.LoginResponseWithToken loginWithRole(String email, String password, UserRole role) {
        // 1. 사용자 조회 (DELETED 제외)
        Users user = userRepository.findByEmailAndRoleInAndStatusNot(email, List.of(role), UserStatus.DELETED)
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
     * 로그인 응답 생성 (내부 전달용)
     * - Access Token 및 Refresh Token 발급
     * - Refresh Token을 저장소에 저장
     * - Controller에서 Cookie 설정 후 LoginResponse로 변환
     */
    private UserDto.LoginResponseWithToken createLoginResponse(Users user, Companies company) {
        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // Refresh Token 저장 (7일 TTL)
        tokenStorageService.saveRefreshToken(
                user.getId(),
                refreshToken,
                604800000L  // 7일 (ms)
        );

        log.info("로그인 성공 - userId: {}, role: {}", user.getId(), user.getRole());

        return UserDto.LoginResponseWithToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompany().getId())
                .companySlug(company != null ? company.getCompanySlug() : null)
                .passwordChangeRequired(user.getIsFirstLogin())
                .build();
    }

    /**
     * Refresh Token으로 Access Token 갱신 (내부 전달용)
     * - Refresh Token 유효성 검증
     * - 저장소에서 토큰 확인
     * - 새로운 Access Token 발급
     * - Controller에서 Cookie 설정 후 RefreshTokenResponse로 변환
     */
    public AuthDto.RefreshTokenResponseWithToken refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 형식 검증
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token 형식");
            throw new RefreshTokenException.InvalidRefreshTokenException();
        }

        // 2. Refresh Token에서 userId 추출
        Long userId = jwtUtil.getUserId(refreshToken);

        // 3. 저장소에서 Refresh Token 확인
        String storedToken = tokenStorageService.getRefreshToken(userId);

        if (storedToken == null) {
            log.warn("저장소에 Refresh Token 없음 - userId: {}", userId);
            throw new RefreshTokenException.RefreshTokenNotFoundException();
        }

        if (!storedToken.equals(refreshToken)) {
            log.warn("Refresh Token 불일치 - userId: {}", userId);
            throw new RefreshTokenException.InvalidRefreshTokenException();
        }

        // 4. 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 5. 계정 상태 검증
        validateUserStatus(user);

        // 6. 새로운 Access Token 생성
        String newAccessToken = jwtUtil.createAccessToken(user);

        // 7. (선택) Refresh Token Rotation - 새 Refresh Token 생성
        // String newRefreshToken = jwtUtil.createRefreshToken(user);
        // tokenStorageService.saveRefreshToken(userId, newRefreshToken, 604800000L);

        log.info("Access Token 갱신 성공 - userId: {}, role: {}", userId, user.getRole());

        return AuthDto.RefreshTokenResponseWithToken.builder()
                .accessToken(newAccessToken)
                .role(user.getRole())  // ← 추가
                // .refreshToken(newRefreshToken)  // Rotation 적용 시
                .userId(userId)
                .message("Access Token이 갱신되었습니다.")
                .build();
    }

    /**
     * 로그아웃
     * - Refresh Token 삭제
     * - 저장소에서 토큰 제거
     */
    public AuthDto.LogoutResponse logout(Long userId) {
        tokenStorageService.deleteRefreshToken(userId);
        log.info("로그아웃 성공 - userId: {}", userId);

        return AuthDto.LogoutResponse.builder()
                .message("로그아웃되었습니다.")
                .logoutAt(LocalDateTime.now())  // ← 추가
                .build();
    }

    /**
     * 비밀번호 변경 시 모든 Refresh Token 삭제
     * - 보안 강화: 비밀번호 변경 시 모든 기기에서 강제 로그아웃
     */
    public void invalidateAllTokens(Long userId) {
        tokenStorageService.deleteAllRefreshTokens(userId);
        log.info("모든 Refresh Token 삭제 - userId: {}", userId);
    }
}
