package org.example.unibooker.domain.user.service;

import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.Company;
import org.example.unibooker.domain.company.model.CompanyDto;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.*;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.infrastructure.email.EmailService;
import org.example.unibooker.utils.FileUploadUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    // 1. 필드 선언
    private final SignUp signUpService;
    private final Approval approvalService;
    private final ManagerManagement managerManagement;

    // 2. 생성자 (수정됨)
    public AdminService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        PasswordEncoder passwordEncoder,
                        FileUploadUtil fileUploadUtil,
                        EmailService emailService) {
        this.signUpService = new SignUp(userRepository, companyRepository, passwordEncoder, fileUploadUtil);
        this.approvalService = new Approval(companyRepository, userRepository, passwordEncoder, emailService);  // ✅ 4개 파라미터
        this.managerManagement = new ManagerManagement(userRepository, companyRepository, passwordEncoder, emailService);
    }

    // ========== 회원가입 관련 ==========

    /**
     * 관리자 회원가입 처리
     */
    @Transactional(readOnly = true)
    public static class SignUp {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final PasswordEncoder passwordEncoder;
        private final FileUploadUtil fileUploadUtil;

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
         * 관리자 회원가입 신청
         */
        @Transactional
        public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request, MultipartFile logoFile) {
            // 이메일 중복 체크
            validateDuplicateEmail(request.getEmail());

            // 기업명 중복 체크
            validateDuplicateCompanyName(request.getCompanyName());

            // Company 생성 및 저장 (먼저 저장해야 ID가 생성됨)
            Company company = createCompany(request, logoFile);
            Company savedCompany = companyRepository.save(company);

            // 임시 비밀번호 생성
            String temporaryPassword = generateTemporaryPassword();
            String encodedPassword = passwordEncoder.encode(temporaryPassword);

            // User 생성 (저장된 company 사용)
            User admin = createAdmin(request, savedCompany, encodedPassword);
            userRepository.save(admin);

            // 응답 DTO 생성
            return AdminDto.SignUpResponse.builder()
                    .message("관리자 회원가입 신청이 완료되었습니다. 승인까지 최대 " + ESTIMATED_APPROVAL_DAYS + "일이 소요될 수 있습니다.")
                    .email(request.getEmail())
                    .companyName(savedCompany.getCompanyName())
                    .estimatedDays(ESTIMATED_APPROVAL_DAYS)
                    .build();
        }

        /**
         * 회원가입 승인 상태 조회
         */
        public AdminDto.StatusResponse checkSignUpStatus(String email) {
            // User 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // Company 조회
            Company company = companyRepository.findById(user.getCompanyId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            // 응답 DTO 생성
            return AdminDto.StatusResponse.builder()
                    .status(company.getStatus())
                    .companyName(company.getCompanyName())
                    .email(user.getEmail())
                    .rejectionReason(company.getRejectionReason())
                    .appliedAt(company.getCreatedAt())
                    .build();
        }

        private Company createCompany(AdminDto.SignUpRequest request, MultipartFile logoFile) {
            String logoUrl = null;
            if (logoFile != null && !logoFile.isEmpty()) {
                logoUrl = fileUploadUtil.uploadCompanyLogo(logoFile);
            }

            return Company.builder()
                    .companyName(request.getCompanyName())
                    .logoUrl(logoUrl)
                    .status(CompanyStatus.PENDING)
                    .build();
        }

        private User createAdmin(AdminDto.SignUpRequest request, Company company, String encodedPassword) {
            return User.builder()
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

        private void validateDuplicateEmail(String email) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);
            }
        }

        private void validateDuplicateCompanyName(String companyName) {
            if (companyRepository.findByCompanyName(companyName).isPresent()) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_COMPANY_NAME);
            }
        }
    }

    // ========== 승인 관리 관련 ==========

    /**
     * 기업 승인 관리
     */
    @Transactional(readOnly = true)
    public static class Approval {

        private final CompanyRepository companyRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        // 임시 비밀번호 생성 관련 상수
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String SPECIAL_CHAR = "@$!%*#?&";
        private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
        private static final int TEMP_PASSWORD_LENGTH = 8;

        public Approval(CompanyRepository companyRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        EmailService emailService) {
            this.companyRepository = companyRepository;
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.emailService = emailService;
        }

        /**
         * 승인 대기 기업 목록 조회
         */
        public List<CompanyDto.PendingResponse> getPendingCompanies() {
            List<Company> pendingCompanies = companyRepository.findByStatus(CompanyStatus.PENDING);

            return pendingCompanies.stream()
                    .map(this::convertToPendingResponse)
                    .collect(Collectors.toList());
        }

        /**
         * 기업 상세 조회
         */
        public CompanyDto.DetailResponse getCompanyDetail(Long companyId) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            User admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            return convertToDetailResponse(company, admin);
        }

        /**
         * 기업 승인 (수정됨)
         */
        @Transactional
        public CompanyDto.ApprovalResponse approveCompany(Long companyId, Long approvedBy) {
            // 1. Company 조회 및 검증
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() == CompanyStatus.APPROVED) {
                throw new BaseException(BaseResponseStatus.ALREADY_APPROVED);
            }

            // 2. Admin User 조회
            User admin = userRepository.findByCompanyIdAndRole(companyId, UserRole.ADMIN)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            // 3. 새로운 임시 비밀번호 생성
            String newTempPassword = generateTemporaryPassword();
            String encodedPassword = passwordEncoder.encode(newTempPassword);

            // 4. Admin User 비밀번호 업데이트
            admin.updatePassword(encodedPassword);

            // 5. Company 승인
            company.approve(approvedBy);

            // 6. Admin User 활성화
            admin.activate();

            // 7. 승인 이메일 발송
            try {
                emailService.sendAdminApprovalEmail(
                        admin.getEmail(),
                        admin.getName(),
                        company.getCompanyName(),
                        newTempPassword
                );
            } catch (Exception e) {
                // 이메일 발송 실패 시 예외 던지기 (트랜잭션 롤백)
                throw new BaseException(BaseResponseStatus.EMAIL_SEND_FAILED);
            }

            // 8. 응답 반환
            return CompanyDto.ApprovalResponse.builder()
                    .message("기업 승인이 완료되었습니다. 승인 이메일이 발송되었습니다.")
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .status(company.getStatus())
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        /**
         * 기업 거절
         */
        @Transactional
        public CompanyDto.ApprovalResponse rejectCompany(Long companyId, String rejectionReason) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() == CompanyStatus.REJECTED) {
                throw new BaseException(BaseResponseStatus.ALREADY_REJECTED);
            }

            company.reject(rejectionReason);

            return CompanyDto.ApprovalResponse.builder()
                    .message("기업 가입 신청이 거절되었습니다.")
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
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

            // 각 문자 종류에서 최소 1개씩 포함
            password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
            password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
            password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
            password.append(SPECIAL_CHAR.charAt(random.nextInt(SPECIAL_CHAR.length())));

            // 나머지 랜덤 채우기
            for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
                password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
            }

            return shuffleString(password.toString(), random);
        }

        /**
         * 문자열 섞기
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

        // ✅ convertToPendingResponse 메서드 (중복 제거)
        private CompanyDto.PendingResponse convertToPendingResponse(Company company) {
            User admin = userRepository.findByCompanyIdAndRole(company.getId(), UserRole.ADMIN)
                    .orElse(null);

            return CompanyDto.PendingResponse.builder()
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .logoUrl(company.getLogoUrl())
                    .adminName(admin != null ? admin.getName() : null)
                    .email(admin != null ? admin.getEmail() : null)
                    .phone(admin != null ? admin.getPhone() : null)
                    .status(company.getStatus())
                    .createdAt(company.getCreatedAt())
                    .build();
        }

        private CompanyDto.DetailResponse convertToDetailResponse(Company company, User admin) {
            return CompanyDto.DetailResponse.builder()
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
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
                    .build();
        }
    }

    // ========== 매니저 관리 ==========

    /**
     * 매니저 계정 관리
     */
    @Transactional(readOnly = true)
    public static class ManagerManagement {

        private final UserRepository userRepository;
        private final CompanyRepository companyRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmailService emailService;

        // 임시 비밀번호 생성 관련 상수
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
         */
        @Transactional
        public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long currentUserId) {
            // 1. 권한 검증
            User admin = validateAdminAuthority(currentUserId);

            // 2. 회사 상태 확인
            Company company = validateCompanyStatus(admin.getCompanyId());

            // 3. 이메일 중복 체크
            validateEmailDuplicate(request.getEmail());

            // 4. 임시 비밀번호 생성
            String temporaryPassword = generateTemporaryPassword();

            // 5. 매니저 계정 생성 및 저장
            User manager = createManagerUser(request, company.getId(), temporaryPassword);

            // 6. 이메일 발송 (동기 처리)
            sendManagerCreationEmail(request, company, temporaryPassword);

            // 7. 응답 반환
            return buildCreateResponse(manager, company);
        }

        private User validateAdminAuthority(Long userId) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

            if (!user.hasAdminAuthority()) {
                throw new BaseException(BaseResponseStatus.UNAUTHORIZED_ACTION);
            }

            return user;
        }

        private Company validateCompanyStatus(Long companyId) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

            if (company.getStatus() != CompanyStatus.APPROVED) {
                throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
            }

            return company;
        }

        private void validateEmailDuplicate(String email) {
            if (userRepository.existsByEmail(email)) {
                throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);
            }
        }

        private User createManagerUser(ManagerDto.CreateRequest request, Long companyId, String temporaryPassword) {
            String encodedPassword = passwordEncoder.encode(temporaryPassword);

            User manager = User.builder()
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .name(request.getName())
                    .phone(request.getPhone())
                    .role(UserRole.MANAGER)
                    .status(UserStatus.INACTIVE)
                    .companyId(companyId)
                    .isFirstLogin(true)
                    .build();

            return userRepository.save(manager);
        }

        private void sendManagerCreationEmail(ManagerDto.CreateRequest request, Company company, String temporaryPassword) {
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

        private ManagerDto.CreateResponse buildCreateResponse(User manager, Company company) {
            return ManagerDto.CreateResponse.builder()
                    .message("매니저 계정이 성공적으로 생성되었습니다. 이메일을 확인해주세요.")
                    .managerId(manager.getId())
                    .email(manager.getEmail())
                    .name(manager.getName())
                    .companyName(company.getCompanyName())
                    .createdAt(manager.getCreatedAt())
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

    // ========== 퍼블릭 메서드 (컨트롤러에서 호출) ==========

    // 회원가입 관련
    public AdminDto.SignUpResponse signUpAdmin(AdminDto.SignUpRequest request, MultipartFile logoFile) {
        return signUpService.signUpAdmin(request, logoFile);
    }

    public AdminDto.StatusResponse checkSignUpStatus(String email) {
        return signUpService.checkSignUpStatus(email);
    }

    // 승인 관리 관련
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

    // 매니저 관리 관련
    public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long currentUserId) {
        return managerManagement.createManager(request, currentUserId);
    }
}