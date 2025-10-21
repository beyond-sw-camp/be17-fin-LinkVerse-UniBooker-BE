package org.example.unibooker.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.dto.AdminDto;
import org.example.unibooker.domain.user.model.dto.ManagerDto;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.example.unibooker.infrastructure.email.EmailService;
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
import java.util.stream.Collectors;

/**
 * 매니저 관리 서비스
 * - 매니저 계정 생성, 조회, 삭제
 * - ADMIN이 자신의 기업 소속 매니저 관리
 * - SUPER가 모든 관리자/매니저 조회 및 상태 변경
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /** 임시 비밀번호 생성용 상수 */
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHAR = "@$!%*#?&";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHAR;
    private static final int TEMP_PASSWORD_LENGTH = 8;

    // ========== 매니저 생성 ==========

    /**
     * 매니저 계정 생성 (ADMIN 전용)
     */
    @Transactional
    public ManagerDto.CreateResponse createManager(ManagerDto.CreateRequest request, Long adminUserId) {
        // 1. Admin 권한 검증
        Users admin = validateAdminAuthority(adminUserId);

        // 2. Company 승인 상태 검증
        Companies company = validateCompanyStatus(admin.getCompanyId());

        // 3. 이메일 중복 검증 (ADMIN, MANAGER와 중복 방지)
        validateEmailDuplicate(request.getEmail());

        // 4. 임시 비밀번호 생성
        String temporaryPassword = generateTemporaryPassword();

        // 5. Manager User 생성
        Users manager = createManagerUser(request, company.getId(), temporaryPassword);

        // 6. 이메일 발송
        sendManagerCreationEmail(request, company, temporaryPassword);

        // 7. 응답 생성
        return buildCreateResponse(manager, company);
    }

    // ========== 매니저 조회 ==========

    /**
     * 관리자가 자신의 기업 소속 매니저 목록 조회
     */
    public ManagerDto.ManagerListResponse getManagers(Long adminUserId, int page, int size) {
        // 1. Admin 권한 검증
        Users admin = validateAdminAuthority(adminUserId);

        // 2. 페이징 처리
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 3. 같은 기업의 매니저 조회
        Page<Users> managerPage = userRepository.findByCompanyIdAndRole(
                admin.getCompanyId(),
                UserRole.MANAGER,
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

    // ========== 매니저 삭제 ==========

    /**
     * 매니저 계정 삭제 (ADMIN 전용)
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

        // 6. 응답 생성
        return ManagerDto.ManagerDeleteResponse.builder()
                .message("매니저 계정이 삭제되었습니다.")
                .managerId(manager.getId())
                .name(manager.getName())
                .email(manager.getEmail())
                .deletedAt(LocalDateTime.now())
                .build();
    }

    // ========== 관리자/매니저 상태 변경 (슈퍼 관리자 전용) ==========

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

    // ========== Private 검증 메서드 ==========

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

        if (company.getStatus() != CompanyStatus.APPROVED) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        return company;
    }

    /**
     * MANAGER 이메일 중복 검증 (ADMIN, MANAGER와 중복 방지)
     */
    private void validateEmailDuplicate(String email) {
        if (userRepository.existsByEmailAndRoleIn(email, List.of(UserRole.ADMIN, UserRole.MANAGER))) {
            throw new BaseException(BaseResponseStatus.DUPLICATE_EMAIL);
        }
    }

    // ========== Private 생성 메서드 ==========

    /**
     * Manager User 엔티티 생성
     */
    private Users createManagerUser(ManagerDto.CreateRequest request, Long companyId, String temporaryPassword) {
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        Users manager = Users.builder()
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

    // ========== Private 변환 메서드 ==========

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

    // ========== Private 유틸리티 메서드 ==========

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
