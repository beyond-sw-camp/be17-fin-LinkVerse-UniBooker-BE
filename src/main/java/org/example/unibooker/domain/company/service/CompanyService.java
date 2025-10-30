package org.example.unibooker.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.constants.ReservedSlugs;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.model.CompanyStatus;
import org.example.unibooker.domain.company.model.dto.CompanyDto;
import org.example.unibooker.domain.company.model.entity.Companies;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 기업 관련 비즈니스 로직 처리 서비스
 * - Company Slug 유효성 검증
 * - Company Slug 중복 확인
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /** Company Slug 유효성 검증 패턴: 소문자, 숫자, 하이픈만 허용 (3-30자) */
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");

    /**
     * Company Slug 사용 가능 여부 확인
     * - 형식 검증
     * - 예약어 검증
     * - 중복 검증
     */
    public CompanyDto.SlugCheckResponse checkSlugAvailability(String slug) {
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
        if (ReservedSlugs.isReserved(slug)) {
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
     * Company Slug로 기업 정보 조회 (일반 사용자용)
     */
    @Transactional(readOnly = true)
    public CompanyDto.PublicInfoResponse getCompanyBySlug(String companySlug) {
        // 1. Company Slug로 기업 조회
        Companies company = companyRepository.findByCompanySlug(companySlug)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 2. 서비스 정지 상태 확인 (우선순위 높음)
        if (company.isSuspended()) {
            throw new BaseException(BaseResponseStatus.COMPANY_SUSPENDED);
        }

        // 3. 승인된 기업인지 확인
        if (!company.isApproved()) {
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

    /**
     * 전체 기업 목록 조회 (페이징 + 필터링)
     * - SUPER 권한 필요
     * - 기업관리 목록용: PENDING 제외 (ACTIVE, SUSPENDED만)
     */
    @Transactional(readOnly = true)
    public CompanyDto.CompanyListResponse getAllCompanies(
            int page, int size, CompanyStatus status, String keyword) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Companies> companyPage;

        // ✅ status가 null이면 ACTIVE, SUSPENDED만 조회 (PENDING 제외)
        if (status == null) {
            companyPage = companyRepository.searchCompaniesExcludingPending(
                    keyword, pageable);
        } else {
            companyPage = companyRepository.searchCompanies(
                    status, keyword, pageable);
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

    /**
     * 기업 상태 변경 (ACTIVE ↔ SUSPENDED)
     * - 기업 정지 시 ACTIVE 관리자만 자동 정지
     * - 기업 활성화 시 기업으로 인해 정지된 관리자만 복구
     */
    @Transactional
    public CompanyDto.StatusUpdateResponse updateCompanyStatus(
            Long companyId, CompanyStatus newStatus) {

        // 1. Company 조회
        Companies company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BaseException(
                        BaseResponseStatus.COMPANY_NOT_FOUND));

        // 2. PENDING, REJECTED는 이 API로 변경 불가
        if (newStatus == CompanyStatus.PENDING ||
                newStatus == CompanyStatus.REJECTED) {
            throw new BaseException(
                    BaseResponseStatus.INVALID_STATUS_CHANGE);
        }

        // 3. ACTIVE가 아니면 SUSPENDED로 변경 불가
        if (newStatus == CompanyStatus.SUSPENDED &&
                company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BaseException(
                    BaseResponseStatus.COMPANY_NOT_ACTIVE);
        }

        CompanyStatus oldStatus = company.getStatus();

        // 4. 상태 변경
        if (newStatus == CompanyStatus.ACTIVE) {
            company.activate();

            // 기업 정지로 인해 정지된 관리자만 복구
            List<Users> suspendedByCompany = userRepository
                    .findByCompany_IdAndRoleInAndSuspendedByCompany(
                            companyId,
                            List.of(UserRole.ADMIN, UserRole.MANAGER),
                            true
                    );
            suspendedByCompany.forEach(Users::restoreByCompany);

        } else if (newStatus == CompanyStatus.SUSPENDED) {
            company.suspend();

            // ACTIVE 상태의 관리자만 정지 (기존 SUSPENDED는 유지)
            List<Users> activeAdmins = userRepository
                    .findByCompany_IdAndRoleInAndStatus(
                            companyId,
                            List.of(UserRole.ADMIN, UserRole.MANAGER),
                            UserStatus.ACTIVE
                    );
            activeAdmins.forEach(Users::suspendByCompany);
        }

        companyRepository.save(company);

        return CompanyDto.StatusUpdateResponse.builder()
                .message("기업 상태가 변경되었습니다.")
                .companyId(companyId)
                .companyName(company.getCompanyName())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Companies -> CompanyInfo DTO 변환
     */
    private CompanyDto.CompanyInfo convertToCompanyInfo(Companies company) {
        Users admin = userRepository.findByCompany_IdAndRole(
                company.getId(), UserRole.ADMIN).orElse(null);

        long managerCount = userRepository.countByCompany_IdAndRole(
                company.getId(), UserRole.MANAGER);

        long userCount = userRepository.countByCompany_IdAndRole(
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
}