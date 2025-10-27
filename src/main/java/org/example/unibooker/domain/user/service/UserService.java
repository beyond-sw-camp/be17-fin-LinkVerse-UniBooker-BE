package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.infrastructure.email.EmailService;
import org.example.unibooker.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private final AuthService authService;
    private final EmailService emailService;

    /**
     * 일반 사용자 회원가입 (기업별)
     * - 탈퇴 계정 재가입 허용
     */
    @Transactional
    public UserDto.SignUpResponse signUpUser(UserDto.SignUpRequest request) {
        // 1. 기업 존재 여부 확인
        Companies company = companyRepository.findById(request.getCompanyId())
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
            user.restore(); // DELETED → ACTIVE 변경, deletedAt = null
            user.updatePassword(passwordEncoder.encode(request.getPassword()));
            user.updateName(request.getName());
            user.updatePhone(request.getPhone());
            user.updateBirthDate(request.getBirthDate());
            user.updateGender(request.getGender());
        } else {
            // 3-2. DELETED 아닌 상태에서 중복 확인
            validateDuplicateEmailInCompany(request.getEmail(), request.getCompanyId());

            // 3-3. 신규 사용자 생성
            user = Users.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .name(request.getName())
                    .phone(request.getPhone())
                    .birthDate(request.getBirthDate())
                    .gender(request.getGender())
                    .companyId(request.getCompanyId())
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .build();
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
        if (userRepository.existsByEmailAndCompanyIdAndRoleAndStatusNot(
                email, companyId, UserRole.USER, UserStatus.DELETED)) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL_IN_COMPANY);
        }
    }

    /**
     * 특정 기업 내에서 이메일 중복 여부 확인
     */
    public boolean existsByEmailAndCompany(String email, Long companyId) {
        // USER Role만 체크하도록 수정
        return userRepository.existsByEmailAndCompanyIdAndRoleAndStatusNot(
                email,
                companyId,
                UserRole.USER,
                UserStatus.DELETED
        );
    }

    /**
     * ADMIN/MANAGER 이메일 중복 확인 (DELETED 제외)
     * - ADMIN, MANAGER와만 중복 체크 (USER 제외)
     * - 탈퇴한 계정(DELETED)은 중복으로 간주하지 않음 (재가입 가능)
     * - 한 이메일로 USER + ADMIN 계정 각각 생성 가능
     */
    public boolean existsByEmailForAdmin(String email) {
        return userRepository.existsByEmailAndRoleInAndStatusNot(
                email,
                List.of(UserRole.ADMIN, UserRole.MANAGER),
                UserStatus.DELETED
        );
    }

    /**
     * 이메일로 가입한 모든 계정 조회
     */
    @Transactional(readOnly = true)
    public List<UserDto.AccountInfo> getAccountsByEmail(String email) {
        List<Users> users = userRepository.findAllByEmail(email);

        return users.stream()
                .map(user -> {
                    // 기업 정보 조회
                    String companyName = null;
                    if (user.getCompanyId() != null) {
                        Companies company = companyRepository.findById(user.getCompanyId())
                                .orElse(null);
                        if (company != null) {
                            companyName = company.getCompanyName();
                        }
                    }

                    return UserDto.AccountInfo.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .name(user.getName())
                            .companyId(user.getCompanyId())
                            .companyName(companyName)
                            .role(user.getRole())
                            .status(user.getStatus())
                            .createdAt(user.getCreatedAt())
                            .build();
                })
                .toList();
    }

    /**
     * 일반 사용자 로그인
     * - AuthService에 위임
     */
    @Transactional(readOnly = true)
    public UserDto.LoginResponseWithToken login(UserDto.LoginRequest request) {
        return authService.loginWithCompany(
                request.getEmail(),
                request.getPassword(),
                request.getCompanyId()
        );
    }

    /**
     * 현재 사용자 정보 조회 (헤더 인증 검증용)
     */
    @Transactional(readOnly = true)
    public UserDto.CurrentUserResponse getCurrentUserInfo(Long userId) {
        // 1. 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 기업 정보 조회
        Companies company = null;
        if (user.getCompanyId() != null) {
            company = companyRepository.findById(user.getCompanyId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));
        }

        // 3. Response 생성
        return UserDto.CurrentUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .companyId(company != null ? company.getId() : null)
                .companySlug(company != null ? company.getCompanySlug() : null)  // ← getSlug() → getCompanySlug()
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    /**
     * 로그아웃
     * - AuthService에 위임
     */
    @Transactional
    public AuthDto.LogoutResponse logout(Long userId) {
        return authService.logout(userId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public UserDto.WithdrawResponse withdraw(Long userId, UserDto.WithdrawRequest request) {
        // 1. 사용자 조회
        Users user = userRepository.findById(userId)
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
        Users user = userRepository.findById(userId)
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
     * 내 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserDto.ProfileResponse getMyProfile(Long userId) {
        // 1. 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 2. 기업명 및 로고 조회
        String companyName = null;
        String businessNumber = null;
        String logoUrl = null;  // ← 추가

        if (user.getCompanyId() != null) {
            Companies company = companyRepository.findById(user.getCompanyId())
                    .orElse(null);
            if (company != null) {
                companyName = company.getCompanyName();
                businessNumber = company.getBusinessNumber();
                logoUrl = company.getLogoUrl();  // ← 추가
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
                .businessNumber(businessNumber)
                .logoUrl(logoUrl)  // ← 추가
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
        Users user = userRepository.findById(userId)
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
    private void validateUserStatus(Users user) {
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

    /**
     * 비밀번호 찾기 - 임시 비밀번호 발급
     */
    @Transactional
    public void resetPassword(String email, Long companyId) {
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

        // 6. 기업 정보 조회
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 7. 이메일 발송
        try {
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getName(),
                    company.getCompanyName(),
                    tempPassword
            );
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 아이디 찾기 - 이메일 조회
     * - 이름 + 전화번호 또는 이름 + 생년월일로 계정 조회
     * - 찾은 이메일을 마스킹 처리하여 반환
     */
    @Transactional(readOnly = true)
    public UserDto.FindEmailResponse findEmail(UserDto.FindEmailRequest request) {
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

        // 2. 생년월일로 찾기 (우선순위 2)
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

    /**
     * 이메일 마스킹 처리
     * - 예: abc123@gmail.com → abc***@gmail.com
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        // 로컬 부분 마스킹
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
        int TEMP_PASSWORD_LENGTH = 8;

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