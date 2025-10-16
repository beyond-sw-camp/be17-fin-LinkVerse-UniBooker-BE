package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.entity.Company;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.entity.User;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 사용자 비즈니스 로직 처리 서비스
 * - 일반 사용자 회원가입, 로그인, 로그아웃
 * - 비밀번호 변경, 프로필 조회/수정
 * - 회원 탈퇴
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final JwtUtil jwtUtil;

    /**
     * 일반 사용자 회원가입
     */
    @Transactional
    public UserDto.SignUpResponse signUpUser(UserDto.SignUpRequest request) {
        validateDuplicateEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // birthDate, gender 추가
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        return UserDto.SignUpResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserDto.LoginResponse login(UserDto.LoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        }

        // 3. 계정 상태 확인
        validateUserStatus(user);

        // 4. JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // 5. 응답 생성
        return UserDto.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .passwordChangeRequired(user.getIsFirstLogin())
                .companyId(user.getCompanyId())
                .build();
    }

    /**
     * 로그아웃
     */
    @Transactional
    public UserDto.LogoutResponse logout(Long userId, UserDto.LogoutRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 리프레시 토큰 무효화 처리 (Redis 등에서 삭제)
        // TODO: Redis에서 refreshToken 제거 로직 구현
        // jwtUtil.invalidateRefreshToken(request.getRefreshToken());

        // 3. 응답 생성
        return UserDto.LogoutResponse.builder()
                .message("로그아웃이 완료되었습니다.")
                .logoutAt(LocalDateTime.now())
                .build();
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public UserDto.WithdrawResponse withdraw(Long userId, UserDto.WithdrawRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        }

        // 3. 회원 탈퇴 처리 (소프트 삭제)
        user.delete();

        // 4. 응답 생성
        return UserDto.WithdrawResponse.builder()
                .message("회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.")
                .email(user.getEmail())
                .withdrawnAt(LocalDateTime.now())
                .build();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, UserDto.PasswordChangeRequest request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BaseException(BaseResponseStatus.INVALID_PASSWORD);
        }

        // 새 비밀번호 확인 일치 여부
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BaseException(BaseResponseStatus.PASSWORD_MISMATCH);
        }

        // 비밀번호 변경
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);

        // 첫 로그인 플래그 해제
        if (user.getIsFirstLogin()) {
            user.completeFirstLogin();
        }
    }

    /**
     * 내 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserDto.ProfileResponse getMyProfile(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 기업명 조회 (ADMIN 또는 MANAGER인 경우)
        String companyName = null;
        if (user.getCompanyId() != null) {
            Company company = companyRepository.findById(user.getCompanyId())
                    .orElse(null);
            if (company != null) {
                companyName = company.getCompanyName();
            }
        }

        // 3. Response 생성
        return UserDto.ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .role(user.getRole())
                .status(user.getStatus())
                .companyId(user.getCompanyId())
                .companyName(companyName)
                .isFirstLogin(user.getIsFirstLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 내 프로필 수정
     */
    @Transactional
    public UserDto.ProfileResponse updateMyProfile(Long userId,
                                                   UserDto.ProfileUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 정보 수정
        user.updateName(request.getName());

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.updatePhone(request.getPhone());
        }

        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            user.updateBirthDate(request.getBirthDate());
        }

        if (request.getGender() != null) {
            user.updateGender(request.getGender());
        }

        // 3. 변경 후 프로필 반환
        return getMyProfile(userId);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 이메일 중복 체크
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);
        }
    }

    /**
     * 사용자 상태 검증
     */
    private void validateUserStatus(User user) {
        // INACTIVE - 관리자는 승인 대기, 일반 사용자는 이메일 인증 대기
        if (user.isInactive()) {
            if (user.isManager() || user.isAdmin()) {
                throw new BaseException(BaseResponseStatus.APPROVAL_PENDING);
            }
            // 일반 사용자의 INACTIVE는 현재 자동 ACTIVE 처리되므로 발생하지 않음
        }

        // SUSPENDED - 정지된 계정
        if (user.isSuspended()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_SUSPENDED);
        }

        // DELETED - 탈퇴한 계정
        if (user.isDeleted()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_DELETED);
        }
    }
}