package org.example.apiapp.domain.user.service;

import org.example.apiapp.domain.user.model.dto.AdminDto;
import org.example.apiapp.domain.company.model.dto.CompanyDto;
import org.example.apiapp.domain.user.model.dto.ManagerDto;
import org.example.common.exception.BaseException;
import org.example.common.base.BaseResponseStatus;
import org.example.apiapp.domain.company.model.entity.Companies;
import org.example.apiapp.domain.company.repository.CompanyRepository;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.user.repository.UserRepository;
import org.example.apiapp.infrastructure.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.example.common.model.CompanyStatus;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

/**
 * 관리자(Admin) 비즈니스 로직 처리 서비스
 * - 관리자 회원가입 및 상태 조회
 * - 기업 승인/거절 관리
 * - 매니저 계정 생성 및 관리
 */
@Slf4j
@Service
public class AdminService {

    private final SignUp signUpService;
    private final Approval approvalService;
    private final ManagerManagement managerManagement;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthService authService;

    /**
     * AdminService 생성자 (DI)
     */
    public AdminService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService,
                        AuthService authService,
                        @Value("${app.base-url:http://localhost:5173}") String baseUrl) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authService = authService;

        this.signUpService = new SignUp(userRepository, companyRepository, passwordEncoder);
        this.approvalService = new Approval(companyRepository, userRepository, passwordEncoder,
                authService, emailService, baseUrl);
        this.managerManagement = new ManagerManagement(userRepository, companyRepository,
                passwordEncoder, emailService);
    }

    // ========== 회원가입 관련 퍼블릭 메서드 ==========

    /**
     * 관리자 회원가입 처리
     */
    public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request) {
        return signUpService.signUpAdmin(request);
    }

    /**
     * 회원가입 신청 상태 조회
     */
    public AdminDto.StatusResponse checkSignUpStatus(String email) {
        return signUpService.checkSignUpStatus(email);
    }

    /**
     * MANAGER를 ADMIN으로 승격 (SUPER 전용)
     */
    public AdminDto.PromoteResponse promoteManagerToAdmin(Long managerId, Long superUserId) {
        return managerManagement.promoteManagerToAdmin(managerId, superUserId);
    }

    // ========== 승인 관리 관련 퍼블릭 메서드 ==========

    /**
     * 승인 대기 중인 기업 목록 조회
     */
    public List<CompanyDto.PendingResponse> getPendingCompanies() {
        return approvalService.getPendingCompanies();
    }

    /**
     * 기업 상세 정보 조회
     */
    public CompanyDto.DetailResponse getCompanyDetail(Long companyId) {
        return approvalService.getCompanyDetail(companyId);
    }

    /**
     * 기업 승인 처리
     */
    public CompanyDto.ApprovalResponse approveCompany(Long companyId, Long approvedBy) {
        return approvalService.approveCompany(companyId, approvedBy);
    }

    /**
     * 기업 거절 처리
     */
    public CompanyDto.ApprovalResponse rejectCompany(Long companyId, String rejectionReason) {
        return approvalService.rejectCompany(companyId, rejectionReason);
    }

    /**
     * 비밀번호 재설정
     */
    public AdminDto.PasswordResetResponse resetPassword(Long userId, AdminDto.PasswordResetRequest request) {
        return approvalService.resetPassword(userId, request);
    }

    // ========== 매니저 관리 관련 퍼블릭 메서드 ==========

    /**
     * 매니저 계정 생성
     */
    public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long currentUserId) {
        return managerManagement.createManager(request, currentUserId);
    }

    /**
     * 관리자의 매니저 목록 조회
     */
    public ManagerDto.ManagerListResponse getManagers(Long userId, int page, int size) {
        return managerManagement.getManagers(userId, page, size);
    }

    /**
     * 매니저 계정 삭제
     */
    public ManagerDto.ManagerDeleteResponse deleteManager(Long managerId, Long currentUserId) {
        return managerManagement.deleteManager(managerId, currentUserId);
    }

    /**
     * 매니저 정보 수정
     */
    public ManagerDto.UpdateResponse updateManager(Long managerId, ManagerDto.UpdateRequest request, Long currentUserId) {
        return managerManagement.updateManager(managerId, request, currentUserId);
    }

    /**
     * 슈퍼관리자가 모든 관리자/매니저 조회
     */
    public AdminDto.AdminListResponse getAllAdmins(int page, int size, UserRole role, UserStatus status) {
        return managerManagement.getAllAdmins(page, size, role, status);
    }

    /**
     * 슈퍼관리자가 관리자/매니저 상태 변경
     */
    public void updateAdminStatus(Long userId, AdminDto.AdminStatusUpdateRequest request) {
        managerManagement.updateAdminStatus(userId, request);
    }

    // ========== 기타 ==========

    /**
     * 관리자/매니저 로그인
     */
    public org.example.apiapp.domain.user.model.dto.UserDto.LoginResponseWithToken adminLogin(
            AdminDto.AdminLoginRequest request) {
        return authService.loginWithRoles(
                request.getEmail(),
                request.getPassword(),
                List.of(UserRole.ADMIN, UserRole.MANAGER)
        );
    }

    /**
     * 기업 로고 업데이트
     */
    @Transactional
    public void updateCompanyLogo(Long userId, String logoUrl) {
        Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER) {
            throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
        }

        Companies company = companyRepository.findByIdAndDeletedAtIsNull(user.getCompanyId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        company.updateLogoUrl(logoUrl);
        companyRepository.save(company);
    }

    // ========== 회원가입 처리 내부 클래스 ==========

    /**
     * 관리자 회원가입 처리 내부 클래스
     * - 관리자 회원가입 신청
     * - 회원가입 상태 조회
     */
    @Transactional(readOnly = true)
    public class SignUp {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final PasswordEncoder passwordEncoder;

        // 상수 정의
        private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String SPECIAL_CHAR = "@$!%*#?&";
        private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
        private static final int TEMP_PASSWORD_LENGTH = 8;
        private static final int ESTIMATED_APPROVAL_DAYS = 3;

        public SignUp(UserRepository userRepository,
                      CompanyRepository companyRepository,
                      PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.companyRepository = companyRepository;
            this.passwordEncoder = passwordEncoder;
        }

        /**
         * 관리자 회원가입 처리
         * - 사업자등록번호, Slug, 이메일 중복 검증
         * - Company 및 Admin User 생성 (PENDING/INACTIVE 상태)
         */
        @Transactional
        public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request) {
            log.info("관리자 회원가입 시작 - email: {}", request.getEmail());

            // 1. 사업자등록번호, Slug 중복 검증
            validateDuplicateBusinessNumber(request.getBusinessNumber());
            validateCompanySlug(request.getCompanySlug());

            // 2. 탈퇴한 ADMIN 계정이 있는지 확인
            Optional<Users> deletedAdmin = userRepository
                    .findByEmailAndRoleAndStatus(request.getEmail(), UserRole.ADMIN, UserStatus.DELETED);

            Companies company;
            Users admin;

            if (deletedAdmin.isPresent()) {
                // 2-1. 탈퇴 ADMIN 계정 복구
                admin = deletedAdmin.get();
                admin.restore();

                // 2-2. 신규 Company 생성
                company = createCompany(request);
                company = companyRepository.save(company);

                // 2-3. 임시 비밀번호 생성 및 정보 업데이트
                String temporaryPassword = generateTemporaryPassword();
                String encodedPassword = passwordEncoder.encode(temporaryPassword);

                admin.updatePassword(encodedPassword);
                admin.updateName(request.getName());
                admin.updatePhone(request.getPhone());
                admin.updateCompanyId(company.getId());
                admin.deactivate();

            } else {
                // 2-4. DELETED 아닌 상태에서 이메일 중복 확인
                validateDuplicateEmail(request.getEmail());

                // 2-5. 신규 Company 및 Admin 생성
                company = createCompany(request);
                company = companyRepository.save(company);

                String temporaryPassword = generateTemporaryPassword();
                String encodedPassword = passwordEncoder.encode(temporaryPassword);

                admin = createAdmin(request, company, encodedPassword);
            }

            userRepository.save(admin);

            // TODO: Kafka로 슈퍼 관리자에게 알림 발송

            return AdminDto.SignUpResponse.builder()
                    .message("관리자 회원가입 신청이 완료되었습니다. 승인까지 최대 " + ESTIMATED_APPROVAL_DAYS + "일이 소요될 수 있습니다.")
                    .email(request.getEmail())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .serviceUrl(null)
                    .estimatedDays(ESTIMATED_APPROVAL_DAYS)
                    .build();
        }

        /**
         * 회원가입 신청 상태 조회
         */
        public AdminDto.StatusResponse checkSignUpStatus(String email) {
            log.info("회원가입 상태 조회 - email: {}", email);

            List<Users> users = userRepository.findByEmailAndRoleIn(
                    email,
                    List.of(UserRole.ADMIN, UserRole.MANAGER)
            );

            if (users.isEmpty()) {
                throw new BaseException(BaseResponseStatus.USER_NOT_FOUND);
            }

            Users user = users.get(0);
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(user.getCompanyId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            return AdminDto.StatusResponse.builder()
                    .status(company.getStatus())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .email(user.getEmail())
                    .rejectionReason(company.getRejectionReason())
                    .appliedAt(company.getCreatedAt())
                    .build();
        }

        /**
         * Company Slug 유효성 검증
         */
        private void validateCompanySlug(String slug) {
            if (!SLUG_PATTERN.matcher(slug).matches()) {
                throw new BaseException(BaseResponseStatus.INVALID_SLUG_FORMAT);
            }

            if (slug.startsWith("-") || slug.endsWith("-")) {
                throw new BaseException(BaseResponseStatus.INVALID_SLUG_FORMAT);
            }

            if (slug.contains("--")) {
                throw new BaseException(BaseResponseStatus.INVALID_SLUG_FORMAT);
            }

            // TODO: ReservedSlugs 체크 (Common에 추가 예정)

            if (companyRepository.existsByCompanySlug(slug)) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_SLUG);
            }
        }

        /**
         * Company 엔티티 생성
         */
        private Companies createCompany(AdminDto.SignUpRequest request) {
            return Companies.builder()
                    .businessNumber(request.getBusinessNumber())
                    .companyName(request.getCompanyName())
                    .companySlug(request.getCompanySlug())
                    .logoUrl(request.getLogoUrl())
                    .status(CompanyStatus.PENDING)
                    .build();
        }

        /**
         * Admin User 엔티티 생성
         */
        private Users createAdmin(AdminDto.SignUpRequest request, Companies company, String encodedPassword) {
            return Users.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .role(UserRole.ADMIN)
                    .status(UserStatus.INACTIVE)
                    .companyId(company.getId())
                    .isFirstLogin(true)
                    .build();
        }

        /**
         * 임시 비밀번호 생성
         */
        private String generateTemporaryPassword() {
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

            password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
            password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
            password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
            password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

            for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
                password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
            }

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

        /**
         * ADMIN/MANAGER 이메일 중복 검증 (DELETED 제외)
         */
        private void validateDuplicateEmail(String email) {
            if (userRepository.existsByEmailAndRoleInAndStatusNot(
                    email,
                    List.of(UserRole.ADMIN, UserRole.MANAGER),
                    UserStatus.DELETED)) {
                throw new BaseException(BaseResponseStatus.ADMIN_MANAGER_EMAIL_EXISTS);
            }
        }

        /**
         * 사업자등록번호 중복 검증
         */
        private void validateDuplicateBusinessNumber(String businessNumber) {
            if (companyRepository.existsByBusinessNumber(businessNumber)) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_BUSINESS_NUMBER);
            }
        }
    }

    // ========== 기업 승인/거절 처리 내부 클래스 ==========

    /**
     * 기업 승인/거절 처리 내부 클래스
     * - 승인 대기 기업 목록 조회
     * - 기업 상세 정보 조회
     * - 기업 승인/거절 처리
     * - 비밀번호 재설정
     */
    @Transactional(readOnly = true)
    public class Approval {

        private final CompanyRepository companyRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final AuthService authService;
        private final String baseUrl;

        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String SPECIAL_CHAR = "@$!%*#?&";
        private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
        private static final int TEMP_PASSWORD_LENGTH = 8;

        public Approval(CompanyRepository companyRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthService authService,
                        EmailService emailService,
                        String baseUrl) {
            this.companyRepository = companyRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.emailService = emailService;
            this.authService = authService;
            this.baseUrl = baseUrl;
        }

        /**
         * 승인 대기 중인 기업 목록 조회
         */
        public List<CompanyDto.PendingResponse> getPendingCompanies() {
            log.info("승인 대기 기업 목록 조회");

            List<Companies> pendingCompanies = companyRepository.findByStatus(CompanyStatus.PENDING);

            return pendingCompanies.stream()
                    .map(this::convertToPendingResponse)
                    .collect(Collectors.toList());
        }

        /**
         * 기업 상세 정보 조회
         */
        public CompanyDto.DetailResponse getCompanyDetail(Long companyId) {
            log.info("기업 상세 정보 조회 - companyId: {}", companyId);

            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            Users admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // TODO: Resource-Service에서 OpenFeign으로 조회
            Long serviceGroupCount = 0L;
            Long userCount = userRepository.countByCompanyIdAndStatusNot(companyId, UserStatus.DELETED);
            LocalDateTime lastLoginAt = null; // TODO: 마지막 로그인 시간 추가

            return CompanyDto.DetailResponse.builder()
                    .companyId(company.getId())
                    .businessNumber(company.getBusinessNumber())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .logoUrl(company.getLogoUrl())
                    .status(company.getStatus())
                    .createdAt(company.getCreatedAt())
                    .approvedAt(company.getApprovedAt())
                    .approvedBy(company.getApprovedBy())
                    .rejectionReason(company.getRejectionReason())
                    .adminId(admin.getId())
                    .adminName(admin.getName())
                    .email(admin.getEmail())
                    .phone(admin.getPhone())
                    .userStatus(admin.getStatus())
                    .serviceGroupCount(serviceGroupCount)
                    .userCount(userCount)
                    .lastLoginAt(lastLoginAt)
                    .build();
        }

        /**
         * 기업 승인 처리
         */
        @Transactional
        public CompanyDto.ApprovalResponse approveCompany(Long companyId, Long approvedBy) {
            log.info("기업 승인 처리 - companyId: {}, approvedBy: {}", companyId, approvedBy);

            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() == CompanyStatus.ACTIVE) {
                throw new BaseException(BaseResponseStatus.ALREADY_APPROVED);
            }

            Users admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            String newTempPassword = generateTemporaryPassword();
            String encodedPassword = passwordEncoder.encode(newTempPassword);

            admin.updatePassword(encodedPassword);
            admin.activate();

            company.approve(approvedBy);

            userRepository.save(admin);
            companyRepository.save(company);

            String serviceUrl = company.getServiceUrl(baseUrl);

            try {
                emailService.sendAdminApprovalEmail(
                        admin.getEmail(),
                        admin.getName(),
                        company.getCompanyName(),
                        newTempPassword,
                        serviceUrl
                );
            } catch (Exception e) {
                log.error("승인 이메일 발송 실패 - email: {}", admin.getEmail(), e);
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }

            return CompanyDto.ApprovalResponse.builder()
                    .message("기업 승인이 완료되었습니다. 승인 이메일이 발송되었습니다.")
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .serviceUrl(serviceUrl)
                    .status(company.getStatus())
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        /**
         * 기업 거절 처리
         */
        @Transactional
        public CompanyDto.ApprovalResponse rejectCompany(Long companyId, String rejectionReason) {
            log.info("기업 거절 처리 - companyId: {}", companyId);

            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() != CompanyStatus.PENDING) {
                throw new BaseException(BaseResponseStatus.INVALID_COMPANY_STATUS);
            }

            Users admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            try {
                emailService.sendCompanyRejectionEmail(
                        admin.getEmail(),
                        admin.getName(),
                        company.getCompanyName(),
                        company.getBusinessNumber(),
                        company.getCreatedAt(),
                        rejectionReason
                );
            } catch (Exception e) {
                log.error("거절 이메일 발송 실패 - email: {}", admin.getEmail(), e);
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }

            company.reject(rejectionReason);
            companyRepository.save(company);

            List<Users> users = userRepository.findByCompanyId(companyId);
            for (Users user : users) {
                user.delete();
            }
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
            }

            return CompanyDto.ApprovalResponse.builder()
                    .message("기업 가입 신청이 거절되었습니다. 거절 사유가 이메일로 발송되었습니다.")
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        /**
         * 비밀번호 재설정
         */
        @Transactional
        public AdminDto.PasswordResetResponse resetPassword(Long userId, AdminDto.PasswordResetRequest request) {
            log.info("비밀번호 재설정 - userId: {}", userId);

            Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BaseException(BaseResponseStatus.CURRENT_PASSWORD_INCORRECT);
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new BaseException(BaseResponseStatus.PASSWORD_MISMATCH);
            }

            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new BaseException(BaseResponseStatus.SAME_PASSWORD);
            }

            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.updatePassword(encodedPassword);

            if (user.getIsFirstLogin()) {
                // TODO: Kafka로 환영 알림 발송
                user.completeFirstLogin();
            }

            authService.invalidateAllTokens(userId);
            userRepository.save(user);

            return AdminDto.PasswordResetResponse.builder()
                    .message("비밀번호가 성공적으로 변경되었습니다.")
                    .passwordChangeRequired(false)
                    .build();
        }

        /**
         * 임시 비밀번호 생성
         */
        private String generateTemporaryPassword() {
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

            password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
            password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
            password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
            password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

            for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
                password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
            }

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

        /**
         * Company -> PendingResponse DTO 변환
         */
        private CompanyDto.PendingResponse convertToPendingResponse(Companies company) {
            Users admin = userRepository.findByCompanyIdAndRole(company.getId(), UserRole.ADMIN)
                    .orElse(null);

            return CompanyDto.PendingResponse.builder()
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .logoUrl(company.getLogoUrl())
                    .adminName(admin != null ? admin.getName() : null)
                    .email(admin != null ? admin.getEmail() : null)
                    .phone(admin != null ? admin.getPhone() : null)
                    .status(company.getStatus())
                    .createdAt(company.getCreatedAt())
                    .build();
        }
    }

    // ========== 매니저 관리 내부 클래스 (기존 코드 유지) ==========

    /**
     * 매니저 관리 처리 내부 클래스
     * - 매니저 생성, 조회, 수정, 삭제
     */
    @Transactional(readOnly = true)
    public class ManagerManagement {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String SPECIAL_CHAR = "@$!%*#?&";
        private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
        private static final int TEMP_PASSWORD_LENGTH = 8;

        public ManagerManagement(UserRepository userRepository,
                                 CompanyRepository companyRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService) {
            this.userRepository = userRepository;
            this.companyRepository = companyRepository;
            this.passwordEncoder = passwordEncoder;
            this.emailService = emailService;
        }

        /**
         * 매니저 계정 생성
         * - 탈퇴한 MANAGER 계정 재활용
         */
        @Transactional
        public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long adminUserId) {
            log.info("매니저 계정 생성 시작 - adminUserId: {}, email: {}", adminUserId, request.getEmail());

            Users admin = validateAdminAuthority(adminUserId);
            Companies company = validateCompanyStatus(admin.getCompanyId());

            // 1. DELETED MANAGER 계정 조회
            Optional<Users> deletedManager = userRepository.findByEmailAndRoleIn(
                            request.getEmail(),
                            List.of(UserRole.ADMIN, UserRole.MANAGER)
                    ).stream()
                    .filter(u -> u.getRole() == UserRole.MANAGER && u.getStatus() == UserStatus.DELETED)
                    .findFirst();

            String temporaryPassword = generateTemporaryPassword();
            Users manager;

            if (deletedManager.isPresent()) {
                // 2. DELETED MANAGER 재활용
                manager = deletedManager.get();
                manager.restore(); // status: DELETED→ACTIVE, deletedAt: NULL

                // 3. 정보 업데이트
                String encodedPassword = passwordEncoder.encode(temporaryPassword);
                manager.updatePassword(encodedPassword);
                manager.updateName(request.getName());
                manager.updatePhone(request.getPhone());
                manager.updateCompanyId(company.getId());

                manager = userRepository.save(manager);
                log.info("DELETED MANAGER 재활용 - managerId: {}, email: {}", manager.getId(), manager.getEmail());
            } else {
                // 4. DELETED 없으면 중복 체크
                validateEmailDuplicate(request.getEmail(), company.getId());

                // 5. 신규 MANAGER 생성
                manager = createManagerUser(request, company, temporaryPassword);
                log.info("신규 MANAGER 생성 - managerId: {}, email: {}", manager.getId(), manager.getEmail());
            }

            sendManagerCreationEmail(request, company, temporaryPassword);

            return buildCreateResponse(manager, company);
        }

        /**
         * 관리자의 매니저 목록 조회 (페이징)
         */
        public ManagerDto.ManagerListResponse getManagers(Long adminUserId, int page, int size) {
            log.info("매니저 목록 조회 - adminUserId: {}, page: {}, size: {}", adminUserId, page, size);

            Users admin = validateAdminAuthority(adminUserId);

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Users> managerPage = userRepository.findByCompanyIdAndRoleAndStatusNot(
                    admin.getCompanyId(),
                    UserRole.MANAGER,
                    UserStatus.DELETED,
                    pageable
            );

            List<ManagerDto.ManagerListResponse.ManagerInfo> managers = managerPage.getContent().stream()
                    .map(this::convertToManagerInfo)
                    .collect(Collectors.toList());

            return ManagerDto.ManagerListResponse.builder()
                    .managers(managers)
                    .totalElements(managerPage.getTotalElements())
                    .totalPages(managerPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(size)
                    .build();
        }

        /**
         * 슈퍼관리자가 모든 관리자/매니저 조회 (페이징)
         */
        public AdminDto.AdminListResponse getAllAdmins(int page, int size, UserRole role, UserStatus status) {
            log.info("전체 관리자/매니저 조회 - page: {}, size: {}, role: {}, status: {}", page, size, role, status);

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            Page<Users> adminPage;

            if (role != null && status != null) {
                adminPage = userRepository.findByRoleAndStatus(role, status, pageable);
            } else if (role != null) {
                adminPage = userRepository.findByRoleAndStatusNot(role, UserStatus.DELETED, pageable);
            } else if (status != null) {
                adminPage = userRepository.findByRoleInAndStatus(
                        List.of(UserRole.ADMIN, UserRole.MANAGER),
                        status,
                        pageable
                );
            } else {
                adminPage = userRepository.findByRoleInAndStatusNot(
                        List.of(UserRole.ADMIN, UserRole.MANAGER),
                        UserStatus.DELETED,
                        pageable
                );
            }

            List<AdminDto.AdminListResponse.AdminInfo> admins = adminPage.getContent().stream()
                    .map(this::convertToAdminInfo)
                    .collect(Collectors.toList());

            return AdminDto.AdminListResponse.builder()
                    .admins(admins)
                    .totalElements(adminPage.getTotalElements())
                    .totalPages(adminPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(size)
                    .build();
        }

        /**
         * 슈퍼관리자가 관리자/매니저 상태 변경
         */
        @Transactional
        public void updateAdminStatus(Long userId, AdminDto.AdminStatusUpdateRequest request) {
            log.info("관리자/매니저 상태 변경 - userId: {}, newStatus: {}", userId, request.getStatus());

            Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.MANAGER) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            switch (request.getStatus()) {
                case ACTIVE:
                    user.activate();
                    break;
                case INACTIVE:
                    user.deactivate();
                    break;
                case SUSPENDED:
                    user.suspend();
                    break;
                case DELETED:
                    user.delete();
                    break;
                default:
                    throw new BaseException(BaseResponseStatus.INVALID_USER_STATUS);
            }

            userRepository.save(user);
        }

        /**
         * 매니저 계정 삭제
         */
        @Transactional
        public ManagerDto.ManagerDeleteResponse deleteManager(Long managerId, Long adminUserId) {
            log.info("매니저 삭제 - managerId: {}, adminUserId: {}", managerId, adminUserId);

            Users admin = validateAdminAuthority(adminUserId);

            Users manager = userRepository.findByIdAndDeletedAtIsNull(managerId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (manager.getRole() != UserRole.MANAGER) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            if (!manager.getCompanyId().equals(admin.getCompanyId())) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            manager.delete();
            userRepository.save(manager);

            return ManagerDto.ManagerDeleteResponse.builder()
                    .message("매니저 계정이 삭제되었습니다.")
                    .managerId(manager.getId())
                    .name(manager.getName())
                    .email(manager.getEmail())
                    .deletedAt(LocalDateTime.now())
                    .build();
        }

        /**
         * 매니저 정보 수정
         */
        @Transactional
        public ManagerDto.UpdateResponse updateManager(Long managerId, ManagerDto.UpdateRequest request, Long adminUserId) {
            log.info("매니저 정보 수정 - managerId: {}, adminUserId: {}", managerId, adminUserId);

            Users admin = validateAdminAuthority(adminUserId);

            Users manager = userRepository.findByIdAndDeletedAtIsNull(managerId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (manager.getRole() != UserRole.MANAGER) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            if (!manager.getCompanyId().equals(admin.getCompanyId())) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            manager.updateName(request.getName());
            manager.updatePhone(request.getPhone());
            userRepository.save(manager);

            return ManagerDto.UpdateResponse.builder()
                    .message("매니저 정보가 성공적으로 수정되었습니다.")
                    .managerId(manager.getId())
                    .name(manager.getName())
                    .email(manager.getEmail())
                    .phone(manager.getPhone())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        /**
         * MANAGER를 ADMIN으로 승격 (SUPER 전용)
         * - 기존 ADMIN을 MANAGER로 강등
         * - MANAGER를 ADMIN으로 승격
         * - Company 상태는 ACTIVE 유지
         */
        @Transactional
        public AdminDto.PromoteResponse promoteManagerToAdmin(Long managerId, Long superUserId) {
            log.info("MANAGER ADMIN 승격 - managerId: {}, superUserId: {}", managerId, superUserId);

            // 1. SUPER 권한 확인
            Users superUser = userRepository.findByIdAndDeletedAtIsNull(superUserId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (superUser.getRole() != UserRole.SUPER) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 2. MANAGER 조회 및 검증
            Users manager = userRepository.findByIdAndDeletedAtIsNull(managerId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (manager.getRole() != UserRole.MANAGER) {
                throw new BaseException(BaseResponseStatus.NOT_MANAGER);
            }

            // 3. Company 조회
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(manager.getCompanyId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            // 4. 기존 ADMIN 조회
            Users currentAdmin = userRepository.findByCompanyIdAndRole(
                            company.getId(), UserRole.ADMIN)
                    .orElse(null);

            CompanyStatus oldCompanyStatus = company.getStatus();

            // 5. MANAGER → ADMIN 승격
            UserRole managerOldRole = manager.getRole();
            manager.updateRole(UserRole.ADMIN);
            userRepository.save(manager);

            // 6. 기존 ADMIN → MANAGER 강등
            if (currentAdmin != null) {
                currentAdmin.updateRole(UserRole.MANAGER);
                userRepository.save(currentAdmin);

                log.info("기존 ADMIN MANAGER 강등 - userId: {}, email: {}",
                        currentAdmin.getId(), currentAdmin.getEmail());
            }

            // 7. Company 상태는 ACTIVE 유지 (변경 없음)
            // ADMIN_PENDING이 없어졌으므로 상태 변경 불필요

            log.info("MANAGER ADMIN 승격 완료 - userId: {}, companyId: {}, 기존 ADMIN 강등: {}",
                    manager.getId(), company.getId(), currentAdmin != null);

            // TODO: 승격된 ADMIN에게 알림
            // TODO: 강등된 ADMIN에게 알림

            return AdminDto.PromoteResponse.builder()
                    .message(currentAdmin != null
                            ? "매니저가 관리자로 승격되었습니다. 기존 관리자는 매니저로 강등되었습니다."
                            : "매니저가 관리자로 승격되었습니다.")
                    .userId(manager.getId())
                    .email(manager.getEmail())
                    .name(manager.getName())
                    .oldRole(managerOldRole)
                    .newRole(UserRole.ADMIN)
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .oldCompanyStatus(oldCompanyStatus)
                    .newCompanyStatus(company.getStatus())
                    .promotedAt(LocalDateTime.now())
                    .build();
        }

        // ========== Private 헬퍼 메서드 ==========

        private Users validateAdminAuthority(Long userId) {
            Users user = userRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (user.getRole() != UserRole.ADMIN) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            return user;
        }

        private Companies validateCompanyStatus(Long companyId) {
            Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() != CompanyStatus.ACTIVE) {
                throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
            }

            return company;
        }

        private void validateEmailDuplicate(String email, Long companyId) {
            if (userRepository.existsByEmailAndRoleInAndStatusNot(
                    email,
                    List.of(UserRole.ADMIN, UserRole.MANAGER),
                    UserStatus.DELETED)) {
                throw new BaseException(BaseResponseStatus.ADMIN_MANAGER_EMAIL_EXISTS);
            }
        }

        private Users createManagerUser(ManagerDto.CreateRequest request, Companies company, String temporaryPassword) {
            String encodedPassword = passwordEncoder.encode(temporaryPassword);

            Users manager = Users.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .role(UserRole.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .companyId(company.getId())
                    .isFirstLogin(true)
                    .build();

            return userRepository.save(manager);
        }

        private void sendManagerCreationEmail(ManagerDto.CreateRequest request, Companies company, String temporaryPassword) {
            try {
                emailService.sendManagerCreationEmail(
                        request.getEmail(),
                        request.getName(),
                        company.getCompanyName(),
                        temporaryPassword
                );
                log.info("매니저 생성 이메일 발송 완료 - email: {}", request.getEmail());
            } catch (Exception e) {
                log.error("매니저 생성 이메일 발송 실패 - email: {}", request.getEmail(), e);
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }
        }

        private ManagerDto.CreateResponse buildCreateResponse(Users manager, Companies company) {
            return ManagerDto.CreateResponse.builder()
                    .message("매니저 계정이 성공적으로 생성되었습니다. 이메일을 확인해주세요.")
                    .managerId(manager.getId())
                    .email(manager.getEmail())
                    .name(manager.getName())
                    .companyName(company.getCompanyName())
                    .createdAt(manager.getCreatedAt())
                    .build();
        }

        private ManagerDto.ManagerListResponse.ManagerInfo convertToManagerInfo(Users manager) {
            return ManagerDto.ManagerListResponse.ManagerInfo.builder()
                    .managerId(manager.getId())
                    .name(manager.getName())
                    .email(manager.getEmail())
                    .phone(manager.getPhone())
                    .status(manager.getStatus())
                    .isFirstLogin(manager.getIsFirstLogin())
                    .createdAt(manager.getCreatedAt())
                    .lastLoginAt(null)
                    .build();
        }

        private AdminDto.AdminListResponse.AdminInfo convertToAdminInfo(Users admin) {
            Companies company = null;
            if (admin.getCompanyId() != null) {
                company = companyRepository.findByIdAndDeletedAtIsNull(admin.getCompanyId()).orElse(null);
            }

            return AdminDto.AdminListResponse.AdminInfo.builder()
                    .userId(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .phone(admin.getPhone())
                    .role(admin.getRole())
                    .status(admin.getStatus())
                    .companyId(admin.getCompanyId())
                    .companyName(company != null ? company.getCompanyName() : null)
                    .companySlug(company != null ? company.getCompanySlug() : null)
                    .createdAt(admin.getCreatedAt())
                    .lastLoginAt(null)
                    .build();
        }

        private String generateTemporaryPassword() {
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);

            password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
            password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
            password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
            password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

            for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
                password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
            }

            return shuffleString(password.toString(), random);
        }

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
}