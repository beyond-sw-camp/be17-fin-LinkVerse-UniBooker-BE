package org.example.unibooker.domain.user.service;

import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.constants.ReservedSlugs;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.user.model.*;
import org.example.unibooker.domain.user.model.dto.AdminDto;
import org.example.unibooker.domain.user.model.dto.ManagerDto;
import org.example.unibooker.domain.user.model.dto.UserDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.infrastructure.email.EmailService;
import org.example.unibooker.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 관리자(Admin) 비즈니스 로직 처리 서비스
 * - 관리자 회원가입 및 상태 조회
 * - 기업 승인/거절 관리
 * - 매니저 계정 생성 및 관리
 */
@Service
public class AdminService {

    private final SignUp signUpService;
    private final Approval approvalService;
    private final ManagerManagement managerManagement;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceGroupRepository resourceGroupRepository;

    /**
     * AdminService 생성자 (DI)
     */
    public AdminService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder,
                        FileUploadUtil fileUploadUtil,
                        EmailService emailService,
                        AuthService authService,
                        ResourceGroupRepository resourceGroupRepository,
                        @Value("${app.base-url:http://localhost:5173}") String baseUrl) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceGroupRepository = resourceGroupRepository;
        this.signUpService = new SignUp(userRepository, companyRepository, passwordEncoder, fileUploadUtil);

        // ✅ 수정: authService와 emailService 순서 변경
        this.approvalService = new Approval(
                companyRepository,
                userRepository,
                passwordEncoder,
                authService,
                emailService,
                resourceGroupRepository,
                baseUrl
        );

        this.managerManagement = new ManagerManagement(userRepository, companyRepository, passwordEncoder, emailService);
        this.authService = authService;
    }

    // ========== 회원가입 관련 ==========

    /**
     * 관리자 회원가입 처리 내부 클래스
     */
    @Transactional(readOnly = true)
    public static class SignUp {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final PasswordEncoder passwordEncoder;
        private final FileUploadUtil fileUploadUtil;

        // 상수 정의
        private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String SPECIAL_CHAR = "@$!%*#?&";
        private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
        private static final int TEMP_PASSWORD_LENGTH = 8;
        private static final int ESTIMATED_APPROVAL_DAYS = 3;

        public SignUp(UserRepository userRepository, CompanyRepository companyRepository,
                      PasswordEncoder passwordEncoder, FileUploadUtil fileUploadUtil) {
            this.userRepository = userRepository;
            this.companyRepository = companyRepository;
            this.passwordEncoder = passwordEncoder;
            this.fileUploadUtil = fileUploadUtil;
        }

        /**
         * 관리자 회원가입 처리
         * - 탈퇴 계정 재가입 허용
         */
        @Transactional
        public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request, MultipartFile logoFile) {
            // 1. 사업자등록번호, Slug 중복 검증
            validateDuplicateBusinessNumber(request.getBusinessNumber());
            validateCompanySlug(request.getCompanySlug());

            // 2. 탈퇴한 ADMIN 계정이 있는지 확인
            Optional<Users> deletedUser = userRepository.findByEmailAndStatus(
                    request.getEmail(),
                    UserStatus.DELETED
            );

            // ADMIN 역할의 탈퇴 계정만 필터링
            Optional<Users> deletedAdmin = deletedUser.filter(Users::isAdmin);

            Companies company;
            Users admin;

            if (deletedAdmin.isPresent()) {
                // 2-1. 탈퇴 ADMIN 계정 복구
                admin = deletedAdmin.get();
                admin.restore(); // DELETED → INACTIVE 변경

                // 2-2. 신규 Company 생성
                company = createCompany(request, logoFile);
                company = companyRepository.save(company);

                // 2-3. 임시 비밀번호 생성 및 정보 업데이트
                String temporaryPassword = generateTemporaryPassword();
                String encodedPassword = passwordEncoder.encode(temporaryPassword);

                admin.updatePassword(encodedPassword);
                admin.updateName(request.getName());
                admin.updatePhone(request.getPhone());
                admin.updateCompanyId(company.getId()); // 새 Company로 연결
                admin.deactivate(); // INACTIVE 상태로 설정 (승인 대기)

            } else {
                // 2-4. DELETED 아닌 상태에서 이메일 중복 확인
                validateDuplicateEmail(request.getEmail());

                // 2-5. 신규 Company 및 Admin 생성
                company = createCompany(request, logoFile);
                company = companyRepository.save(company);

                String temporaryPassword = generateTemporaryPassword();
                String encodedPassword = passwordEncoder.encode(temporaryPassword);

                admin = createAdmin(request, company, encodedPassword);
            }

            userRepository.save(admin);

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
         * - ADMIN/MANAGER 권한만 조회
         */
        public AdminDto.StatusResponse checkSignUpStatus(String email) {
            List<Users> users = userRepository.findByEmailAndRoleIn(
                    email,
                    List.of(UserRole.ADMIN, UserRole.MANAGER)
            );

            if (users.isEmpty()) {
                throw new BaseException(BaseResponseStatus.USER_NOT_FOUND);
            }

            Users user = users.get(0);

            Companies company = companyRepository.findById(user.getCompanyId())
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

            if (ReservedSlugs.isReserved(slug)) {
                throw new BaseException(BaseResponseStatus.RESERVED_SLUG);
            }

            if (companyRepository.existsByCompanySlug(slug)) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_SLUG);
            }
        }

        /**
         * Company 엔티티 생성
         */
        private Companies createCompany(AdminDto.SignUpRequest request, MultipartFile logoFile) {
            String logoUrl = null;
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = fileUploadUtil.uploadCompanyLogo(logoFile);
            }

            return Companies.builder()
                    .businessNumber(request.getBusinessNumber())
                    .companyName(request.getCompanyName())
                    .companySlug(request.getCompanySlug())
                    .logoUrl(logoUrl)
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
                throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);
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

    // ========== 승인 관리 관련 ==========

    /**
     * 기업 승인/거절 처리 내부 클래스
     */
    @Transactional(readOnly = true)
    public static class Approval {

        private final CompanyRepository companyRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;
        private final AuthService authService;
        private final ResourceGroupRepository resourceGroupRepository;
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
                        ResourceGroupRepository resourceGroupRepository,
                        String baseUrl) {
            this.companyRepository = companyRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.emailService = emailService;
            this.authService = authService;
            this.resourceGroupRepository = resourceGroupRepository;
            this.baseUrl = baseUrl;
        }

        /**
         * 승인 대기 중인 기업 목록 조회
         */
        public List<CompanyDto.PendingResponse> getPendingCompanies() {
            List<Companies> pendingCompanies = companyRepository.findByStatus(CompanyStatus.PENDING);

            return pendingCompanies.stream()
                    .map(this::convertToPendingResponse)
                    .collect(Collectors.toList());
        }

        /**
         * 기업 상세 정보 조회
         * - 기업 정보 + 관리자 정보 + 플랫폼 이용 현황
         */
        public CompanyDto.DetailResponse getCompanyDetail(Long companyId) {
            // 1. 기업 조회
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            // 2. 관리자 조회
            Users admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 3. 플랫폼 이용 현황 데이터 조회
            Long serviceGroupCount = resourceGroupRepository.countByCompanyId(companyId);
            Long userCount = userRepository.countUsersByCompanyId(companyId);
            LocalDateTime lastLoginAt = userRepository.findLastLoginByCompanyId(companyId);

            // 4. DTO 변환 및 반환
            return CompanyDto.DetailResponse.builder()
                    // 기업 정보
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

                    // 관리자 정보
                    .adminId(admin.getId())
                    .adminName(admin.getName())
                    .email(admin.getEmail())
                    .phone(admin.getPhone())
                    .userStatus(admin.getStatus())

                    // 플랫폼 이용 현황
                    .serviceGroupCount(serviceGroupCount != null ? serviceGroupCount : 0L)
                    .userCount(userCount != null ? userCount : 0L)
                    .lastLoginAt(lastLoginAt)

                    .build();
        }

        /**
         * 기업 승인 처리
         * - Company 상태를 APPROVED로 변경
         * - Admin User 상태를 INACTIVE → ACTIVE로 변경
         * - 새로운 임시 비밀번호 생성 및 이메일 발송
         */
        @Transactional
        public CompanyDto.ApprovalResponse approveCompany(Long companyId, Long approvedBy) {
            // 1. Company 조회
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            // 2. 중복 승인 방지
            if (company.getStatus() == CompanyStatus.ACTIVE) {
                throw new BaseException(BaseResponseStatus.ALREADY_APPROVED);
            }

            // 3. Admin User 조회
            Users admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 4. 새로운 임시 비밀번호 생성
            String newTempPassword = generateTemporaryPassword();
            String encodedPassword = passwordEncoder.encode(newTempPassword);

            // 5. 비밀번호 및 상태 업데이트 (중요: 반드시 activate() 호출)
            admin.updatePassword(encodedPassword);
            admin.activate();  // INACTIVE -> ACTIVE 변경

            // 6. Company 승인 처리
            company.approve(approvedBy);

            // 7. 명시적 저장 (JPA 더티 체킹 보장)
            userRepository.save(admin);
            companyRepository.save(company);

            // 8. 서비스 URL 생성
            String serviceUrl = company.getServiceUrl(baseUrl);

            // 9. 승인 이메일 발송
            try {
                emailService.sendAdminApprovalEmail(
                        admin.getEmail(),
                        admin.getName(),
                        company.getCompanyName(),
                        newTempPassword,
                        serviceUrl
                );
            } catch (Exception e) {
                // 이메일 발송 실패 시에도 승인은 완료되어야 함
                // 로깅 후 예외를 던지면 트랜잭션이 롤백되므로 주의 필요
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }

            // 10. 응답 생성
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
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() == CompanyStatus.REJECTED) {
                throw new BaseException(BaseResponseStatus.ALREADY_REJECTED);
            }

            company.reject(rejectionReason);

            return CompanyDto.ApprovalResponse.builder()
                    .message("기업 가입 신청이 거절되었습니다.")
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .companySlug(company.getCompanySlug())
                    .status(company.getStatus())
                    .processedAt(LocalDateTime.now())
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
         * 비밀번호 재설정
         * - 임시 비밀번호 검증
         * - 새 비밀번호 유효성 검증
         * - 비밀번호 변경 및 isFirstLogin 플래그 해제
         * - 모든 Refresh Token 삭제 (보안 강화)  ← 추가
         */
        @Transactional
        public AdminDto.PasswordResetResponse resetPassword(Long userId, AdminDto.PasswordResetRequest request) {
            // 1. 사용자 조회
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 2. 현재 비밀번호 검증 (임시 비밀번호 확인)
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BaseException(BaseResponseStatus.CURRENT_PASSWORD_INCORRECT);
            }

            // 3. 새 비밀번호와 확인 비밀번호 일치 여부 확인
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new BaseException(BaseResponseStatus.PASSWORD_MISMATCH);
            }

            // 4. 새 비밀번호가 현재 비밀번호와 동일한지 확인
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new BaseException(BaseResponseStatus.SAME_PASSWORD);
            }

            // 5. 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());

            // 6. 비밀번호 업데이트 + isFirstLogin 플래그 해제
            user.updatePassword(encodedPassword);
            user.completeFirstLogin();

            // 7. 비밀번호 변경 후 모든 Refresh Token 삭제 (보안 강화) ← 추가
            authService.invalidateAllTokens(userId);

            // 8. 명시적 저장
            userRepository.save(user);

            // 9. 응답 생성
            return AdminDto.PasswordResetResponse.builder()
                    .message("비밀번호가 성공적으로 변경되었습니다.")
                    .passwordChangeRequired(false)
                    .build();
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

    // ========== 매니저 관리 ==========

    /**
     * 매니저 계정 생성 및 관리 내부 클래스
     */
    @Transactional(readOnly = true)
    public static class ManagerManagement {

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
         * - 같은 기업의 DELETED MANAGER 계정이 있으면 재활용
         * - 없으면 신규 생성
         */
        @Transactional
        public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long currentUserId) {
            // 1. Admin 권한 검증
            Users admin = validateAdminAuthority(currentUserId);

            // 2. Company 승인 상태 검증
            Companies company = validateCompanyStatus(admin.getCompanyId());

            // 3. 이메일 중복 검증 (같은 기업 내)
            validateEmailDuplicate(request.getEmail(), admin.getCompanyId());

            // 4. 같은 기업의 DELETED MANAGER 계정 찾기
            Optional<Users> deletedManager = userRepository.findByEmailAndCompanyIdAndRoleAndStatus(
                    request.getEmail(),
                    admin.getCompanyId(),
                    UserRole.MANAGER,
                    UserStatus.DELETED
            );

            Users manager;
            String temporaryPassword = generateTemporaryPassword();

            if (deletedManager.isPresent()) {
                // 5-1. DELETED MANAGER 재활용
                manager = deletedManager.get();
                manager.restore(); // DELETED → ACTIVE

                String encodedPassword = passwordEncoder.encode(temporaryPassword);
                manager.updatePassword(encodedPassword);
                manager.updateName(request.getName());
                manager.updatePhone(request.getPhone());

                userRepository.save(manager);
            } else {
                // 5-2. 신규 MANAGER 생성
                manager = createManagerUser(request, company.getId(), temporaryPassword);
            }

            // 6. 이메일 발송
            sendManagerCreationEmail(request, company, temporaryPassword);

            // 7. 응답 생성
            return buildCreateResponse(manager, company);
        }

        /**
         * 관리자가 자신의 기업 소속 매니저 목록 조회 (DELETED 제외)
         */
        public ManagerDto.ManagerListResponse getManagers(Long adminUserId, int page, int size) {
            // 1. Admin 권한 검증
            Users admin = validateAdminAuthority(adminUserId);

            // 2. 페이징 처리
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 3. 같은 기업의 매니저 조회 (DELETED 제외)
            Page<Users> managerPage = userRepository.findByCompanyIdAndRoleAndStatusNot(
                    admin.getCompanyId(),
                    UserRole.MANAGER,
                    UserStatus.DELETED,
                    pageable
            );

            // 4. DTO 변환
            List<ManagerDto.ManagerListResponse.ManagerInfo> managers = managerPage.getContent().stream()
                    .map(this::convertToManagerInfo)
                    .collect(Collectors.toList());

            // 5. 응답 생성
            return ManagerDto.ManagerListResponse.builder()
                    .managers(managers)
                    .totalElements(managerPage.getTotalElements())
                    .totalPages(managerPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(size)
                    .build();
        }

        /**
         * 슈퍼관리자가 모든 관리자+매니저 조회
         */
        public AdminDto.AdminListResponse getAllAdmins(int page, int size, UserRole role, UserStatus status) {
            // 1. 페이징 처리
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            // 2. 조회 조건에 따라 분기
            Page<Users> adminPage;

            if (role != null && status != null) {
                // 권한 + 상태 둘 다 필터링
                adminPage = userRepository.findByRoleAndStatus(role, status, pageable);
            } else if (role != null) {
                // 권한만 필터링
                adminPage = userRepository.findByRole(role, pageable);
            } else if (status != null) {
                // 상태만 필터링
                adminPage = userRepository.findByStatus(status, pageable);
            } else {
                // 전체 조회 (ADMIN, MANAGER, SUPER만)
                adminPage = userRepository.findByRoleIn(
                        List.of(UserRole.ADMIN, UserRole.MANAGER, UserRole.SUPER),
                        pageable
                );
            }

            // 3. DTO 변환
            List<AdminDto.AdminListResponse.AdminInfo> admins = adminPage.getContent().stream()
                    .map(this::convertToAdminInfo)
                    .collect(Collectors.toList());

            // 4. 응답 생성
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
            // 1. 사용자 조회
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 2. 권한 확인 (ADMIN 또는 MANAGER만)
            if (!user.hasAdminAuthority() && !user.isManager()) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 3. 상태 변경
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
        }

        /**
         * Admin 권한 검증
         */
        private Users validateAdminAuthority(Long userId) {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (!user.hasAdminAuthority()) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            return user;
        }

        /**
         * Company 승인 상태 검증
         */
        private Companies validateCompanyStatus(Long companyId) {
            Companies company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() != CompanyStatus.ACTIVE) {
                throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
            }

            return company;
        }

        /**
         * MANAGER 이메일 중복 검증 (같은 기업 내에서만)
         * - 같은 기업 내 MANAGER 중복 체크 (DELETED 제외)
         * - 같은 기업 내 ADMIN 역할 충돌 체크 (DELETED 제외)
         * - 다른 기업의 계정은 체크하지 않음 (멀티테넌트)
         */
        private void validateEmailDuplicate(String email, Long companyId) {
            // 1. 전체 시스템에서 ADMIN/MANAGER 이메일 중복 체크 (DELETED 제외)
            if (userRepository.existsByEmailAndRoleInAndStatusNot(
                    email,
                    List.of(UserRole.ADMIN, UserRole.MANAGER),
                    UserStatus.DELETED)) {
                throw new BaseException(BaseResponseStatus.ADMIN_MANAGER_EMAIL_EXISTS);
            }

            // 2. USER 역할과는 공존 가능하므로 별도 체크 불필요
        }

        /**
         * Manager User 엔티티 생성
         * - 즉시 사용 가능하도록 ACTIVE 상태로 생성
         */
        private Users createManagerUser(ManagerDto.CreateRequest request, Long companyId, String temporaryPassword) {
            String encodedPassword = passwordEncoder.encode(temporaryPassword);

            Users manager = Users.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .role(UserRole.MANAGER)
                    .status(UserStatus.ACTIVE)
                    .companyId(companyId)
                    .isFirstLogin(true)
                    .build();

            return userRepository.save(manager);
        }

        /**
         * 매니저 생성 이메일 발송
         */
        private void sendManagerCreationEmail(ManagerDto.CreateRequest request, Companies company, String temporaryPassword) {
            try {
                emailService.sendManagerCreationEmail(
                        request.getEmail(),
                        request.getName(),
                        company.getCompanyName(),
                        temporaryPassword
                );
            } catch (Exception e) {
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }
        }

        /**
         * 매니저 계정 삭제
         */
        @Transactional
        public ManagerDto.ManagerDeleteResponse deleteManager(Long managerId, Long adminUserId) {
            // 1. Admin 권한 검증
            Users admin = validateAdminAuthority(adminUserId);

            // 2. 매니저 조회
            Users manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 3. 매니저 권한 확인
            if (!manager.isManager()) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 4. 같은 회사 소속인지 확인
            if (!manager.getCompanyId().equals(admin.getCompanyId())) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 5. 매니저 삭제 처리
            manager.delete();

            // 6. 명시적 저장 (추가 필요)
            userRepository.save(manager);

            // 7. 응답 생성
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
            // 1. Admin 권한 검증
            Users admin = validateAdminAuthority(adminUserId);

            // 2. 매니저 조회
            Users manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 3. 매니저 권한 확인
            if (!manager.isManager()) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 4. 같은 회사 소속인지 확인
            if (!manager.getCompanyId().equals(admin.getCompanyId())) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            // 5. 정보 업데이트
            manager.updateName(request.getName());
            manager.updatePhone(request.getPhone());

            // 6. 명시적 저장
            userRepository.save(manager);

            // 7. 응답 생성
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
         * CreateResponse DTO 생성
         */
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

        /**
         * User -> ManagerInfo DTO 변환
         */
        private ManagerDto.ManagerListResponse.ManagerInfo convertToManagerInfo(Users manager) {
            return ManagerDto.ManagerListResponse.ManagerInfo.builder()
                    .managerId(manager.getId())
                    .name(manager.getName())
                    .email(manager.getEmail())
                    .phone(manager.getPhone())
                    .status(manager.getStatus())
                    .isFirstLogin(manager.getIsFirstLogin())
                    .createdAt(manager.getCreatedAt())
                    .lastLoginAt(null) // TODO: 마지막 로그인 시간 추가 시 구현
                    .build();
        }

        /**
         * User -> AdminInfo DTO 변환
         */
        private AdminDto.AdminListResponse.AdminInfo convertToAdminInfo(Users admin) {
            Companies company = null;
            if (admin.getCompanyId() != null) {
                company = companyRepository.findById(admin.getCompanyId()).orElse(null);
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
                    .lastLoginAt(null) // TODO: 마지막 로그인 시간 추가 시 구현
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
    }

    // ========== 퍼블릭 메서드 (컨트롤러에서 호출) ==========

    public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request, MultipartFile logoFile) {
        return signUpService.signUpAdmin(request, logoFile);
    }

    public AdminDto.StatusResponse checkSignUpStatus(String email) {
        return signUpService.checkSignUpStatus(email);
    }

    public List<CompanyDto.PendingResponse> getPendingCompanies() {
        return approvalService.getPendingCompanies();
    }

    public CompanyDto.DetailResponse getCompanyDetail(Long companyId) {
        return approvalService.getCompanyDetail(companyId);
    }

    public CompanyDto.ApprovalResponse approveCompany(Long companyId, Long approvedBy) {
        return approvalService.approveCompany(companyId, approvedBy);
    }

    public CompanyDto.ApprovalResponse rejectCompany(Long companyId, String rejectionReason) {
        return approvalService.rejectCompany(companyId, rejectionReason);
    }

    public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long currentUserId) {
        return managerManagement.createManager(request, currentUserId);
    }

    /**
     * 관리자/매니저 로그인
     * - AuthService에 위임
     */
    public UserDto.LoginResponseWithToken adminLogin(AdminDto.AdminLoginRequest request) {
        return authService.loginWithRoles(
                request.getEmail(),
                request.getPassword(),
                List.of(UserRole.ADMIN, UserRole.MANAGER)
        );
    }

    /**
     * 관리자의 매니저 목록 조회
     */
    public ManagerDto.ManagerListResponse getManagers(Long userId, int page, int size) {
        return managerManagement.getManagers(userId, page, size);
    }

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

    /**
     * 비밀번호 재설정
     * - Approval 클래스의 resetPassword 메서드에 위임
     */
    public AdminDto.PasswordResetResponse resetPassword(Long userId, AdminDto.PasswordResetRequest request) {
        return approvalService.resetPassword(userId, request);
    }
}