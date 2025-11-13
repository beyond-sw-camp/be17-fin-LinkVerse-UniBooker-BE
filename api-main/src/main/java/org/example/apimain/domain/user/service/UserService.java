package org.example.apimain.domain.user.service;

import org.example.apimain.domain.user.model.dto.UserDto;
import org.example.common.exception.BaseException;
import org.example.common.base.BaseResponseStatus;
import org.example.apimain.domain.company.model.entity.Companies;
import org.example.apimain.domain.company.repository.CompanyRepository;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;
import org.example.apimain.domain.user.model.entity.Users;
import org.example.apimain.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 사용자 서비스
 * - 일반 사용자 회원가입, 로그인, 프로필 관리
 * - 비밀번호 관리, 회원 탈퇴
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    // private final EmailService emailService; // 이메일 서비스 (추후 구현)

    private static final int TEMP_PASSWORD_LENGTH = 8;

    // ========== 회원가입 ==========

    /**
     * 일반 사용자 회원가입 (기업별)
     * - 탈퇴 계정 재가입 허용
     */
    @Transactional
    public UserDto.SignUpResponse signUpUser(UserDto.SignUpRequest request) {
        log.info("일반 사용자 회원가입 - email: {}, companyId: {}", request.getEmail(), request.getCompanyId());

        // 1. 기업 존재 여부 확인
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(request.getCompanyId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 2. 기업이 승인된 상태인지 확인
        if (!company.isApproved()) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 3. 탈퇴한 계정이 있는지 확인
        Optional<Users> deletedUser = userRepository.findByEmailAndCompanyIdAndRoleAndStatus(
                request.getEmail(),
                request.getCompanyId(),
                UserRole.USER,
                UserStatus.DELETED
        );

        Users user;
        if (deletedUser.isPresent()) {
            // 3-1. 탈퇴 계정이 있으면 복구 및 정보 업데이트
            user = deletedUser.get();
            user.restore();
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
            user.updateName(request.getName());
            if (request.getPhone() != null) {
                user.updatePhone(request.getPhone());
            }
            if (request.getBirthDate() != null) {
                user.updateBirthDate(request.getBirthDate());
            }
            if (request.getGender() != null) {
                user.updateGender(request.getGender());
            }
        } else {
            // 3-2. DELETED 아닌 상태에서 중복 확인
            validateDuplicateEmailInCompany(request.getEmail(), request.getCompanyId());

            // 3-3. 신규 사용자 생성
            user = Users.createWithCompany(
                    request.getEmail(),
                    passwordEncoder.encode(request.getPassword()),
                    request.getName(),
                    company.getId(),
                    UserRole.USER
            );
            if (request.getPhone() != null) {
                user.updatePhone(request.getPhone());
            }
            if (request.getBirthDate() != null) {
                user.updateBirthDate(request.getBirthDate());
            }
            if (request.getGender() != null) {
                user.updateGender(request.getGender());
            }
        }

        Users savedUser = userRepository.save(user);

        return UserDto.SignUpResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .companyId(savedUser.getCompanyId())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    /**
     * 특정 기업 내에서 USER 이메일 중복 확인 (DELETED 제외)
     */
    private void validateDuplicateEmailInCompany(String email, Long companyId) {
        boolean exists = userRepository.existsByEmailAndCompanyIdAndRoleAndStatusNot(
                email,
                companyId,
                UserRole.USER,
                UserStatus.DELETED
        );
        if (exists) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL_IN_COMPANY);
        }
    }

    // ========== 로그인 ==========

    /**
     * 일반 사용자 로그인
     * - AuthService에 위임
     */
    public UserDto.LoginResponseWithToken login(UserDto.LoginRequest request) {
        log.info("일반 사용자 로그인 - email: {}, companyId: {}", request.getEmail(), request.getCompanyId());

        return authService.loginWithCompany(
                request.getEmail(),
                request.getPassword(),
                request.getCompanyId()
        );
    }

    // ========== 현재 사용자 정보 ==========

    /**
     * 현재 사용자 정보 조회 (헤더 인증 검증용)
     */
    public UserDto.CurrentUserResponse getCurrentUserInfo(Long userId) {
        log.info("현재 사용자 정보 조회 - userId: {}", userId);

        // 1. 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 기업 정보 조회
        Long companyId = user.getCompanyId();
        String companySlug = null;

        if (companyId != null) {
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));
            companySlug = company.getCompanySlug();
        }

        // 3. Response 생성
        return UserDto.CurrentUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .companyId(companyId)
                .companySlug(companySlug)
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    // ========== 비밀번호 관리 ==========

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, UserDto.PasswordChangeRequest request) {
        log.info("비밀번호 변경 - userId: {}", userId);

        // 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
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

        // 비밀번호 변경 시 모든 Refresh Token 삭제 (보안 강화)
        authService.invalidateAllTokens(userId);
    }

    /**
     * 비밀번호 찾기 - 임시 비밀번호 발급
     */
    @Transactional
    public void resetPassword(String email, Long companyId) {
        log.info("비밀번호 찾기 - email: {}, companyId: {}", email, companyId);

        // 1. 사용자 조회 (USER 역할만)
        Users user = userRepository.findByEmailAndCompanyIdAndRoleAndStatusNot(
                email,
                companyId,
                UserRole.USER,
                UserStatus.DELETED
        ).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 계정 상태 확인
        if (user.isSuspended()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_SUSPENDED);
        }

        // 3. 임시 비밀번호 생성
        String tempPassword = generateTemporaryPassword();

        // 4. 비밀번호 암호화 및 저장
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // 5. 기업 정보 조회
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 6. 이메일 발송
        // TODO: EmailService 구현 후 활성화
        log.info("임시 비밀번호 발송 - email: {}, tempPassword: {}", email, tempPassword);
        // emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), company.getCompanyName(), tempPassword);
    }

    // ========== 프로필 관리 ==========

    /**
     * 내 프로필 조회
     */
    public UserDto.ProfileResponse getMyProfile(Long userId) {
        log.info("프로필 조회 - userId: {}", userId);

        // 1. 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 기업 정보 조회
        Long companyId = user.getCompanyId();
        String companyName = null;
        String businessNumber = null;
        String logoUrl = null;

        if (companyId != null) {
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));
            companyName = company.getCompanyName();
            businessNumber = company.getBusinessNumber();
            logoUrl = company.getLogoUrl();
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
                .companyId(companyId)
                .companyName(companyName)
                .businessNumber(businessNumber)
                .logoUrl(logoUrl)
                .isFirstLogin(user.getIsFirstLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 내 프로필 수정
     */
    @Transactional
    public UserDto.ProfileResponse updateMyProfile(Long userId, UserDto.ProfileUpdateRequest request) {
        log.info("프로필 수정 - userId: {}", userId);

        // 1. 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
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

    // ========== 회원 탈퇴 ==========

    /**
     * 회원 탈퇴
     */
    @Transactional
    public UserDto.WithdrawResponse withdraw(Long userId, UserDto.WithdrawRequest request) {
        log.info("회원 탈퇴 - userId: {}", userId);

        // 1. 사용자 조회
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
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

    // ========== 이메일 관리 ==========

    /**
     * 특정 기업 내에서 이메일 중복 여부 확인
     */
    public boolean existsByEmailAndCompany(String email, Long companyId) {
        return userRepository.existsByEmailAndCompanyIdAndRoleAndStatusNot(
                email,
                companyId,
                UserRole.USER,
                UserStatus.DELETED
        );
    }

    /**
     * 이메일로 가입한 모든 계정 조회
     */
    public List<UserDto.AccountInfo> getAccountsByEmail(String email) {
        log.info("이메일로 가입한 계정 조회 - email: {}", email);

        List<Users> users = userRepository.findAllByEmail(email);

        return users.stream()
                .map(user -> {
                    // 기업 정보 조회
                    String companyName = null;
                    Long companyId = user.getCompanyId();

                    if (companyId != null) {
                        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId).orElse(null);
                        if (company != null) {
                            companyName = company.getCompanyName();
                        }
                    }

                    return UserDto.AccountInfo.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .companyId(companyId)
                            .companyName(companyName)
                            .role(user.getRole())
                            .status(user.getStatus())
                            .createdAt(user.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 아이디 찾기 - 이메일 조회
     */
    public UserDto.FindEmailResponse findEmail(UserDto.FindEmailRequest request) {
        log.info("아이디 찾기 - name: {}, phone: {}", request.getName(), request.getPhone());

        Users user = null;

        // 1. 전화번호로 찾기 (우선순위 1)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user = userRepository.findByNameAndCompanyIdAndPhoneAndRoleAndStatusNot(
                    request.getName(),
                    request.getCompanyId(),
                    request.getPhone(),
                    UserRole.USER,
                    UserStatus.DELETED
            ).orElse(null);
        }

        // 2. 생년월일로 찾기
        if (user == null && request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            user = userRepository.findByNameAndCompanyIdAndBirthDateAndRoleAndStatusNot(
                    request.getName(),
                    request.getCompanyId(),
                    request.getBirthDate(),
                    UserRole.USER,
                    UserStatus.DELETED
            ).orElse(null);
        }

        // 3. 계정을 찾지 못한 경우
        if (user == null) {
            throw new BaseException(BaseResponseStatus.USER_NOT_FOUND);
        }

        // 4. 계정 상태 확인
        if (user.isSuspended()) {
            throw new BaseException(BaseResponseStatus.ACCOUNT_SUSPENDED);
        }

        // 5. 이메일 마스킹 처리
        String maskedEmail = maskEmail(user.getEmail());

        // 6. 응답 생성
        return UserDto.FindEmailResponse.builder()
                .maskedEmail(maskedEmail)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 이메일 마스킹 처리
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        int visibleLength = Math.min(3, localPart.length());
        String visiblePart = localPart.substring(0, visibleLength);
        String maskedPart = "***";

        return visiblePart + maskedPart + "@" + domain;
    }

    /**
     * 임시 비밀번호 생성
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String SPECIAL_CHAR = "@$!%*#?&";
        String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;

        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

        // 각 타입별 최소 1개씩 포함
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

        // 나머지 랜덤 생성
        for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }

        // 문자열 섞기
        return shuffleString(password.toString(), random);
    }

    /**
     * 문자열 무작위 섞기
     */
    private String shuffleString(String input, SecureRandom random) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}