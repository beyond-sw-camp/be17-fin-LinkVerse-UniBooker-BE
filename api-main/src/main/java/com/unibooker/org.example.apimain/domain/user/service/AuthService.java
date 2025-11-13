package com.unibooker.main.domain.user.service;

import com.unibooker.common.util.JwtUtil;
import com.unibooker.main.domain.company.model.entity.Companies;
import com.unibooker.main.domain.user.model.dto.AuthDto;
import com.unibooker.main.domain.user.model.entity.Users;
import com.unibooker.main.domain.company.repository.CompanyRepository;
import com.unibooker.main.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unibooker.common.enums.CompanyStatus;
import com.unibooker.common.enums.Gender;
import com.unibooker.common.enums.UserRole;

/**
 * 인증 서비스
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
     * 로그인
     */
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        log.info("로그인 시도: {}", request.getEmail());

        // 사용자 조회
        Users user = userRepository
                .findByEmailAndCompanyIdAndDeletedAtIsNull(request.getEmail(), request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 계정 상태 확인
        if (!user.isActive()) {
            throw new IllegalArgumentException("활성화되지 않은 계정입니다.");
        }

        // JWT 토큰 생성 (수정된 부분)
        String accessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        return AuthDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .build();
    }

    /**
     * 일반 사용자 회원가입
     */
    @Transactional
    public AuthDto.SignUpResponse signUp(AuthDto.SignUpRequest request) {
        log.info("회원가입: {}", request.getEmail());

        // 이메일 중복 확인
        if (userRepository.existsByEmailAndCompanyIdAndDeletedAtIsNull(request.getEmail(), request.getCompanyId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 기업 확인
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("기업을 찾을 수 없습니다."));

        if (!company.isApproved()) {
            throw new IllegalArgumentException("승인되지 않은 기업입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 성별 변환
        Gender gender = null;
        if (request.getGender() != null) {
            gender = Gender.valueOf(request.getGender().toUpperCase());
        }

        // 사용자 생성
        Users user = Users.createWithCompany(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getCompanyId(),
                UserRole.USER
        );
        user.updatePhone(request.getPhone());
        user.updateBirthDate(request.getBirthDate());
        user.updateGender(gender);

        Users saved = userRepository.save(user);

        return AuthDto.SignUpResponse.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .name(saved.getName())
                .role(saved.getRole())
                .message("회원가입이 완료되었습니다.")
                .build();
    }

    /**
     * 관리자 회원가입
     */
    @Transactional
    public AuthDto.SignUpResponse adminSignUp(AuthDto.AdminSignUpRequest request) {
        log.info("관리자 회원가입: {}", request.getEmail());

        // 사업자등록번호 중복 확인
        if (companyRepository.existsByBusinessNumberAndDeletedAtIsNull(request.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        // Slug 중복 확인
        if (companyRepository.existsByCompanySlugAndDeletedAtIsNull(request.getCompanySlug())) {
            throw new IllegalArgumentException("이미 사용 중인 기업 Slug입니다.");
        }

        // 기업 생성
        Companies company = Companies.builder()
                .businessNumber(request.getBusinessNumber())
                .companyName(request.getCompanyName())
                .companySlug(request.getCompanySlug())
                .status(CompanyStatus.PENDING)
                .build();
        Companies savedCompany = companyRepository.save(company);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 관리자 사용자 생성
        Users admin = Users.createWithCompany(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                savedCompany.getId(),
                UserRole.ADMIN
        );
        admin.updatePhone(request.getPhone());

        Users savedAdmin = userRepository.save(admin);

        return AuthDto.SignUpResponse.builder()
                .userId(savedAdmin.getId())
                .email(savedAdmin.getEmail())
                .name(savedAdmin.getName())
                .role(savedAdmin.getRole())
                .message("관리자 회원가입이 완료되었습니다. 승인을 기다려주세요.")
                .build();
    }

    /**
     * 토큰 갱신
     */
    public AuthDto.LoginResponse refreshToken(String refreshToken) {
        log.info("토큰 갱신");

        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 사용자 ID 추출 (수정된 부분)
        Long userId = jwtUtil.getUserId(refreshToken);

        // 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새 토큰 생성 (수정된 부분)
        String newAccessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId()
        );
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        return AuthDto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .build();
    }

    /**
     * 토큰 갱신 (쿠키 방식용 - 권한 정보 포함 반환)
     */
    public com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken refreshTokenWithRole(String refreshToken) {
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
                user.getCompanyId()
        );
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        // ✅ companySlug 제거 (토큰 갱신 시 불필요)
        return com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .companyId(user.getCompanyId())
                .companySlug(null)  // ← null로 설정 (필요 없음)
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    public com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken loginWithRoles(  // ✅ Main-Service
                                                                                                    String email,
                                                                                                    String password,
                                                                                                    java.util.List<UserRole> allowedRoles) {

        log.info("역할 제한 로그인 시도 - email: {}, allowedRoles: {}", email, allowedRoles);

        // 이메일로 사용자 조회 (기업 ID 없이)
        Users user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new com.unibooker.common.exception.BaseException(
                        com.unibooker.common.exception.BaseResponseStatus.USER_NOT_FOUND));

        // 역할 확인
        if (!allowedRoles.contains(user.getRole())) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.INVALID_PASSWORD);
        }

        // 계정 상태 확인
        if (!user.isActive()) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.INACTIVE_USER);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        // ✅ Main-Service DTO 사용, Enum 변환 제거
        return com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())  // Main-Service Enum 그대로 사용
                .companyId(user.getCompanyId())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    /**
     * 로그아웃
     * - 현재는 Stateless JWT 사용으로 로그만 기록
     * - 추후 Redis 기반 Refresh Token 저장소 구현 시 실제 토큰 삭제
     */
    @Transactional
    public com.unibooker.main.domain.user.model.dto.AuthDto.LogoutResponse logout(Long userId) {
        log.info("로그아웃 - userId: {}", userId);

        // TODO: Redis 기반 Refresh Token 삭제
        // tokenStorageService.deleteRefreshToken(userId);

        return com.unibooker.main.domain.user.model.dto.AuthDto.LogoutResponse.builder()
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

    /**
     * 일반 사용자 로그인 (이메일 + 기업 ID)
     * - USER 역할만 허용
     */
    public com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken loginWithCompany(
            String email,
            String password,
            Long companyId) {

        log.info("일반 사용자 로그인 시도 - email: {}, companyId: {}", email, companyId);

        // 1. 사용자 조회 (USER role, 기업 ID로 조회)
        Users user = userRepository.findByEmailAndCompanyIdAndDeletedAtIsNull(email, companyId)
                .orElseThrow(() -> new com.unibooker.common.exception.BaseException(
                        com.unibooker.common.exception.BaseResponseStatus.USER_NOT_FOUND));

        // 2. USER 역할 확인
        if (user.getRole() != UserRole.USER) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        // 3. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.INVALID_PASSWORD);
        }

        // 4. 계정 상태 확인
        if (!user.isActive()) {
            if (user.isSuspended()) {
                throw new com.unibooker.common.exception.BaseException(
                        com.unibooker.common.exception.BaseResponseStatus.ACCOUNT_SUSPENDED);
            }
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.INACTIVE_USER);
        }

        // 5. 기업 승인 상태 확인
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new com.unibooker.common.exception.BaseException(
                        com.unibooker.common.exception.BaseResponseStatus.COMPANY_NOT_FOUND));

        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new com.unibooker.common.exception.BaseException(
                    com.unibooker.common.exception.BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 6. JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompanyId()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), user.getEmail());

        // 7. Main-Service DTO 반환 (✅ 수정: Main-Service의 UserDto 사용)
        return com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken.builder()
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
}