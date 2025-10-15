package org.example.unibooker.domain.company.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.constants.ReservedSlugs;
import org.example.unibooker.domain.company.model.CompanyDto;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

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
}