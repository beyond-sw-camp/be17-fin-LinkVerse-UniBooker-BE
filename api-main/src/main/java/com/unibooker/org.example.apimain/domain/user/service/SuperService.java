package com.unibooker.main.domain.user.service;

import com.unibooker.main.domain.user.model.dto.SuperDto;
import com.unibooker.common.exception.BaseException;
import com.unibooker.common.exception.BaseResponseStatus;
import com.unibooker.main.domain.company.model.entity.Companies;
import com.unibooker.main.domain.company.repository.CompanyRepository;
import com.unibooker.common.enums.UserRole;
import com.unibooker.main.domain.user.model.entity.Users;
import com.unibooker.main.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 슈퍼 관리자 서비스
 * - 슈퍼 관리자 로그인
 * - 기업 관리자 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    /**
     * 슈퍼 관리자 로그인
     * - 이메일로만 조회, SUPER 권한만
     */
    public com.unibooker.main.domain.user.model.dto.UserDto.LoginResponseWithToken superLogin(SuperDto.SuperLoginRequest request) {
        log.info("슈퍼 관리자 로그인 시도 - email: {}", request.getEmail());

        return authService.loginWithRoles(
                request.getEmail(),
                request.getPassword(),
                List.of(UserRole.SUPER)
        );
    }

    /**
     * 특정 기업의 관리자 목록 조회
     */
    public SuperDto.CompanyManagerListResponse getCompanyManagers(Long companyId) {
        log.info("기업 관리자 목록 조회 - companyId: {}", companyId);

        // 기업 존재 확인
        Companies company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));

        // 해당 기업의 ADMIN, MANAGER 조회
        List<Users> managers = userRepository.findByCompanyIdAndRoleAndDeletedAtIsNull(
                companyId,
                UserRole.ADMIN
        );

        List<Users> managerList = userRepository.findByCompanyIdAndRoleAndDeletedAtIsNull(
                companyId,
                UserRole.MANAGER
        );

        managers.addAll(managerList);

        // DTO 변환
        List<SuperDto.CompanyManagerInfo> managerInfos = managers.stream()
                .map(this::convertToManagerInfo)
                .collect(Collectors.toList());

        return SuperDto.CompanyManagerListResponse.builder()
                .managers(managerInfos)
                .build();
    }

    /**
     * Users -> CompanyManagerInfo 변환
     */
    private SuperDto.CompanyManagerInfo convertToManagerInfo(Users user) {
        // Main-Service UserRole, UserStatus → Common Enum 변환
        com.unibooker.common.enums.UserRole commonRole =
                com.unibooker.common.enums.UserRole.valueOf(user.getRole().name());

        com.unibooker.common.enums.UserStatus commonStatus =
                com.unibooker.common.enums.UserStatus.valueOf(user.getStatus().name());

        return SuperDto.CompanyManagerInfo.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(commonRole)
                .status(commonStatus)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}