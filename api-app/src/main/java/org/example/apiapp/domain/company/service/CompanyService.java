package org.example.apiapp.domain.company.service;

import org.example.common.exception.BaseException;
import org.example.common.base.BaseResponseStatus;
import org.example.apiapp.domain.company.model.dto.CompanyDto;
import org.example.apiapp.domain.company.model.entity.Companies;
import org.example.apiapp.domain.company.repository.CompanyRepository;
import org.example.apiapp.domain.user.model.entity.Users;
import org.example.apiapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.example.common.model.CompanyStatus;
import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

/**
 * 기업 관련 비즈니스 로직 처리 서비스
 * - Company Slug 유효성 검증
 * - Company Slug 중복 확인
 * - 기업 정보 조회
 * - 기업 목록 관리
 * - 기업 상태 변경
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /** Company Slug 유효성 검증 패턴: 소문자, 숫자, 하이픈만 허용 (3-30자) */
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");

    /** 예약된 Slug 목록 */
    private static final Set<String> RESERVED_SLUGS = new HashSet<>(Arrays.asList(
            // API 엔드포인트
            "api", "admin", "super", "manager", "user", "auth", "login", "logout",
            "signup", "register", "dashboard", "profile", "settings",

            // 시스템 페이지
            "error", "404", "500", "maintenance", "status", "health",

            // 공통 경로
            "assets", "static", "public", "resources", "images", "css", "js",
            "favicon", "robots", "sitemap",

            // 회사/조직 관련
            "about", "contact", "support", "help", "terms", "privacy", "policy",

            // 기타 예약어
            "test", "demo", "example", "sample", "temp", "tmp",
            "www", "mail", "ftp", "smtp", "pop", "imap",
            "localhost", "dev", "development", "staging", "production"
    ));

    // ========== Company Slug 검증 ==========

    /**
     * Company Slug 사용 가능 여부 확인
     * - 형식 검증
     * - 예약어 검증
     * - 중복 검증
     */
    public CompanyDto.SlugCheckResponse checkSlugAvailability(String slug) {
        log.info("Company Slug 사용 가능 여부 확인: {}", slug);

        // 1. 형식 검증
        if (!isValidSlugFormat(slug)) {
            return CompanyDto.SlugCheckResponse.builder()
                    .exists(null)
                    .available(false)
                    .message("올바른 형식이 아닙니다. 소문자, 숫자, 하이픈(-)만 사용 가능하며 3~30자여야 합니다.")
                    .slug(slug)
                    .build();
        }

        // 2. 시작/끝 하이픈 검증
        if (slug.startsWith("-") || slug.endsWith("-")) {
            return CompanyDto.SlugCheckResponse.builder()
                    .exists(null)
                    .available(false)
                    .message("하이픈(-)으로 시작하거나 끝날 수 없습니다.")
                    .slug(slug)
                    .build();
        }

        // 3. 연속 하이픈 검증
        if (slug.contains("--")) {
            return CompanyDto.SlugCheckResponse.builder()
                    .exists(null)
                    .available(false)
                    .message("하이픈(-)을 연속해서 사용할 수 없습니다.")
                    .slug(slug)
                    .build();
        }

        // 4. 예약어 검증
        if (isReservedSlug(slug)) {
            return CompanyDto.SlugCheckResponse.builder()
                    .exists(true)
                    .available(false)
                    .message("사용할 수 없는 예약어입니다.")
                    .slug(slug)
                    .build();
        }

        // 5. 중복 검증
        boolean exists = companyRepository.existsByCompanySlug(slug);

        return CompanyDto.SlugCheckResponse.builder()
                .exists(exists)
                .available(!exists)
                .message(exists ? "이미 사용 중인 URL입니다." : "사용 가능한 URL입니다.")
                .slug(slug)
                .build();
    }

    /**
     * Slug 형식 검증
     */
    private boolean isValidSlugFormat(String slug) {
        if (slug == null || slug.isEmpty()) {
            return false;
        }
        return SLUG_PATTERN.matcher(slug).matches();
    }

    /**
     * 예약어 검증
     */
    private boolean isReservedSlug(String slug) {
        return RESERVED_SLUGS.contains(slug.toLowerCase());
    }

    // ========== 기업 정보 조회 ==========

    /**
     * Company Slug로 기업 정보 조회 (일반 사용자용)
     */
    public CompanyDto.PublicInfoResponse getCompanyBySlug(String companySlug) {
        log.info("Company Slug로 기업 정보 조회: {}", companySlug);

        // 1. Company Slug로 기업 조회
        Companies company = companyRepository.findByCompanySlugAndDeletedAtIsNull(companySlug)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 2. 서비스 정지 상태 확인 (우선순위 높음)
        if (company.getStatus() == CompanyStatus.SUSPENDED) {
            throw new BaseException(BaseResponseStatus.COMPANY_SUSPENDED);
        }

        // 3. 승인된 기업인지 확인
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_APPROVED);
        }

        // 4. 공개 정보 반환
        return CompanyDto.PublicInfoResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .companySlug(company.getCompanySlug())
                .logoUrl(company.getLogoUrl())
                .build();
    }

    // ========== 기업 목록 관리 (SUPER 전용) ==========

    /**
     * 전체 기업 목록 조회 (페이징 + 필터링)
     * - SUPER 권한 필요
     * - 기업관리 목록용: PENDING 제외 (ACTIVE, SUSPENDED만)
     */
    public CompanyDto.CompanyListResponse getAllCompanies(
            int page, int size, CompanyStatus status, String keyword) {

        log.info("전체 기업 목록 조회 - page: {}, size: {}, status: {}, keyword: {}",
                page, size, status, keyword);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Companies> companyPage;

        // status가 null이면 ACTIVE, SUSPENDED만 조회 (PENDING 제외)
        if (status == null) {
            // PENDING, REJECTED 제외하고 조회
            if (keyword != null && !keyword.isBlank()) {
                companyPage = companyRepository.findByStatusInAndCompanyNameContainingAndDeletedAtIsNull(
                        List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED),
                        keyword,
                        pageable
                );
            } else {
                companyPage = companyRepository.findByStatusInAndDeletedAtIsNull(
                        List.of(CompanyStatus.ACTIVE, CompanyStatus.SUSPENDED),
                        pageable
                );
            }
        } else {
            // 특정 status로 조회
            if (keyword != null && !keyword.isBlank()) {
                companyPage = companyRepository.findByStatusAndCompanyNameContainingAndDeletedAtIsNull(
                        status,
                        keyword,
                        pageable
                );
            } else {
                companyPage = companyRepository.findByStatusAndDeletedAtIsNull(
                        status,
                        pageable
                );
            }
        }

        List<CompanyDto.CompanyInfo> companies = companyPage.getContent()
                .stream()
                .map(this::convertToCompanyInfo)
                .collect(Collectors.toList());

        return CompanyDto.CompanyListResponse.builder()
                .companies(companies)
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    // ========== 기업 상태 변경 ==========

    /**
     * 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     * - 기업 정지 시 ACTIVE 관리자만 자동 정지
     * - 기업 활성화 시 기업으로 인해 정지된 관리자만 복구
     */
    @Transactional
    public CompanyDto.StatusUpdateResponse updateCompanyStatus(
            Long companyId, CompanyStatus newStatus) {

        log.info("기업 상태 변경 - companyId: {}, newStatus: {}", companyId, newStatus);

        // 1. Company 조회
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 2. PENDING, REJECTED는 이 API로 변경 불가
        if (newStatus == CompanyStatus.PENDING || newStatus == CompanyStatus.REJECTED) {
            throw new BaseException(BaseResponseStatus.INVALID_STATUS_CHANGE);
        }

        // 3. ACTIVE가 아니면 SUSPENDED로 변경 불가
        if (newStatus == CompanyStatus.SUSPENDED &&
                company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BaseException(BaseResponseStatus.COMPANY_NOT_ACTIVE);
        }

        CompanyStatus oldStatus = company.getStatus();  // ✅ Main-Service Enum

        // 4. 상태 변경
        if (newStatus == CompanyStatus.ACTIVE) {
            company.activate();

            // 기업 정지로 인해 정지된 모든 사용자 복구 (역할 무관)
            List<Users> suspendedByCompany = userRepository
                    .findByCompanyIdAndSuspendedByCompany(companyId, true);
            suspendedByCompany.forEach(Users::restoreByCompany);

        } else if (newStatus == CompanyStatus.SUSPENDED) {
            company.suspend();

            // ACTIVE 상태의 모든 사용자 정지 (역할 무관)
            List<Users> activeUsers = userRepository
                    .findByCompanyIdAndStatus(companyId, UserStatus.ACTIVE);
            activeUsers.forEach(Users::suspendByCompany);
        }

        companyRepository.save(company);

        return CompanyDto.StatusUpdateResponse.builder()
                .message("기업 상태가 변경되었습니다.")
                .companyId(companyId)
                .companyName(company.getCompanyName())
                .oldStatus(oldStatus)   // ✅ Main-Service Enum 그대로 사용
                .newStatus(newStatus)   // ✅ Main-Service Enum 그대로 사용
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== 배치 작업 ==========

    /**
     * 거절된 기업 자동 정리 (배치용)
     * - 생성 후 설정된 일수 경과한 REJECTED 기업 삭제
     * - 연결된 사용자도 함께 하드 삭제
     */
    @Transactional
    public void cleanupRejectedCompanies(int retentionDays) {
        log.info("거절된 기업 자동 정리 시작 - retentionDays: {}", retentionDays);

        // 1. 삭제 대상 기업 조회
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

        List<Companies> rejectedCompanies = companyRepository
                .findByStatusAndCreatedAtBefore(CompanyStatus.REJECTED, cutoffTime);

        if (rejectedCompanies.isEmpty()) {
            log.info("삭제 대상 기업 없음");
            return;
        }

        log.info("삭제 대상 기업 수: {}", rejectedCompanies.size());

        // 2. 각 기업별로 연결된 계정 삭제
        for (Companies company : rejectedCompanies) {
            deleteCompanyWithUsers(company);
        }

        log.info("거절된 기업 자동 정리 완료");
    }

    /**
     * 기업 및 연결된 계정 삭제 처리
     */
    private void deleteCompanyWithUsers(Companies company) {
        try {
            // 1. 기업 연결 모든 User 조회
            List<Users> users = userRepository.findByCompanyId(company.getId());

            // 2. User 하드 삭제
            if (!users.isEmpty()) {
                userRepository.deleteAll(users);
                log.info("Company {} 관련 User {} 명 삭제 완료", company.getId(), users.size());
            }

            // 3. Company 하드 삭제
            companyRepository.delete(company);
            log.info("Company {} 삭제 완료", company.getId());

        } catch (Exception e) {
            // 개별 기업 삭제 실패 시 로깅 후 계속 진행
            log.error("Company {} 삭제 실패: {}", company.getId(), e.getMessage());
        }
    }

    // ========== DTO 변환 ==========

    /**
     * Companies -> CompanyInfo DTO 변환
     */
    private CompanyDto.CompanyInfo convertToCompanyInfo(Companies company) {
        Users admin = userRepository.findByCompanyIdAndRole(
                company.getId(), UserRole.ADMIN).orElse(null);

        long managerCount = userRepository.countByCompanyIdAndRole(
                company.getId(), UserRole.MANAGER);

        long userCount = userRepository.countByCompanyIdAndRole(
                company.getId(), UserRole.USER);

        return CompanyDto.CompanyInfo.builder()
                .companyId(company.getId())
                .companyName(company.getCompanyName())
                .companySlug(company.getCompanySlug())
                .logoUrl(company.getLogoUrl())
                .status(company.getStatus())
                .adminName(admin != null ? admin.getName() : null)
                .adminEmail(admin != null ? admin.getEmail() : null)
                .managerCount(managerCount)
                .userCount(userCount)
                .createdAt(company.getCreatedAt())
                .approvedAt(company.getApprovedAt())
                .build();
    }

    // ========== 내부 API용 메서드 ==========

    /**
     * Company Slug로 상태만 조회 (Gateway 전용)
     */
    public CompanyDto.StatusOnlyResponse getCompanyStatusBySlug(String companySlug) {
        log.info("[내부 API] Company 상태 조회 - companySlug: {}", companySlug);

        Companies company = companyRepository.findByCompanySlugAndDeletedAtIsNull(companySlug)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        return CompanyDto.StatusOnlyResponse.builder()
                .companyId(company.getId())
                .companySlug(company.getCompanySlug())
                .status(company.getStatus())
                .build();
    }

    public CompanyDto.StatisticsResponse getStatisticsByYear(int year) {

        long registeredCompanies = companyRepository.countCompaniesByYear(year);
        long registeredCustomers = userRepository.countUsersByYear(year);

        long activeCompanies = companyRepository.countAllByStatus(CompanyStatus.ACTIVE);
        long activeCustomers = userRepository.countAllByRoleAndStatus(UserRole.USER, UserStatus.ACTIVE);

        return CompanyDto.StatisticsResponse.builder()
                .year(year)
                .registeredCompanies(registeredCompanies)
                .registeredCustomers(registeredCustomers)
                .activeCompanies(activeCompanies)
                .activeCustomers(activeCustomers)
                .build();
    }



    public int getUserCountByCompanyId(Long companyId) {
        return companyRepository.countUsersByCompanyId(companyId);
    }
}