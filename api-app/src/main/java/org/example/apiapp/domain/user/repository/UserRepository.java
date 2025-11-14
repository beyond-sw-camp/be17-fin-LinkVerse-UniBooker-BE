package org.example.apiapp.domain.user.repository;

import org.example.apiapp.domain.user.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.common.model.UserRole;
import org.example.common.model.UserStatus;

/**
 * 사용자 레포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    // ========== 기본 조회 ==========

    /**
     * 이메일로 사용자 조회 (삭제 제외)
     */
    Optional<Users> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 이메일과 기업 ID로 사용자 조회 (삭제 제외)
     */
    Optional<Users> findByEmailAndCompanyIdAndDeletedAtIsNull(String email, Long companyId);

    /**
     * ID로 조회 (삭제 제외)
     */
    Optional<Users> findByIdAndDeletedAtIsNull(Long id);

    // ========== 중복 확인 ==========

    /**
     * 이메일 중복 확인 (기업별)
     */
    boolean existsByEmailAndCompanyIdAndDeletedAtIsNull(String email, Long companyId);

    /**
     * ADMIN/MANAGER 이메일 존재 여부 확인 (특정 상태 제외)
     */
    boolean existsByEmailAndRoleInAndStatusNot(String email, List<UserRole> roles, UserStatus status);

    // ========== AdminService용 추가 메서드 ==========

    /**
     * 이메일 + 역할 + 상태로 조회
     * - SignUp에서 탈퇴 ADMIN 찾기용
     */
    Optional<Users> findByEmailAndRoleAndStatus(String email, UserRole role, UserStatus status);

    /**
     * 이메일 + 여러 역할로 조회 (List 반환)
     * - SignUp에서 상태 조회용
     */
    List<Users> findByEmailAndRoleIn(String email, List<UserRole> roles);

    /**
     * 기업 ID + 역할로 조회 (단건)
     * - Approval에서 기업의 ADMIN 조회용
     */
    Optional<Users> findByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 기업 ID로 모든 유저 조회 (List 반환)
     * - Approval에서 기업 거절 시 모든 유저 삭제용
     */
    List<Users> findByCompanyId(Long companyId);

    /**
     * 기업별 사용자 수 카운트 (특정 상태 제외)
     * - Approval에서 기업 상세 정보 조회용
     */
    Long countByCompanyIdAndStatusNot(Long companyId, UserStatus status);

    // ========== 기존 메서드 유지 ==========

    /**
     * 기업별 사용자 목록 조회 (삭제 제외)
     */
    List<Users> findByCompanyIdAndDeletedAtIsNull(Long companyId);

    /**
     * 기업별 특정 권한 사용자 목록 조회 (삭제 제외)
     */
    List<Users> findByCompanyIdAndRoleAndDeletedAtIsNull(Long companyId, UserRole role);

    // ========== 매니저 관리용 메서드 ==========

    /**
     * 기업별 + 역할별 사용자 조회 (특정 상태 제외, 페이징)
     */
    Page<Users> findByCompanyIdAndRoleAndStatusNot(Long companyId, UserRole role, UserStatus status, Pageable pageable);

    /**
     * 역할 + 상태로 조회 (페이징)
     */
    Page<Users> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 역할별 조회 (특정 상태 제외, 페이징)
     */
    Page<Users> findByRoleAndStatusNot(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 여러 역할 + 상태로 조회 (페이징)
     */
    Page<Users> findByRoleInAndStatus(List<UserRole> roles, UserStatus status, Pageable pageable);

    /**
     * 여러 역할 조회 (특정 상태 제외, 페이징)
     */
    Page<Users> findByRoleInAndStatusNot(List<UserRole> roles, UserStatus status, Pageable pageable);

    // ========== UserService용 추가 메서드 ==========

    /**
     * 이메일로 모든 사용자 조회 (여러 기업에 가입한 경우)
     */
    List<Users> findAllByEmail(String email);

    /**
     * 이메일 + 기업 + 역할 + 상태로 조회 (탈퇴 계정 찾기)
     */
    Optional<Users> findByEmailAndCompanyIdAndRoleAndStatus(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 이메일 + 기업 + 역할 중복 확인 (특정 상태 제외)
     */
    boolean existsByEmailAndCompanyIdAndRoleAndStatusNot(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 이메일 + 기업 + 역할로 조회 (특정 상태 제외) - 비밀번호 찾기용
     */
    Optional<Users> findByEmailAndCompanyIdAndRoleAndStatusNot(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 이름 + 기업 + 전화번호 + 역할로 조회 (특정 상태 제외) - 아이디 찾기용
     */
    Optional<Users> findByNameAndCompanyIdAndPhoneAndRoleAndStatusNot(
            String name,
            Long companyId,
            String phone,
            UserRole role,
            UserStatus status
    );

    /**
     * 이름 + 기업 + 생년월일 + 역할로 조회 (특정 상태 제외) - 아이디 찾기용
     */
    Optional<Users> findByNameAndCompanyIdAndBirthDateAndRoleAndStatusNot(
            String name,
            Long companyId,
            String birthDate,
            UserRole role,
            UserStatus status
    );

    /**
     * 기업별 + 역할별 + suspendedByCompany 조건으로 사용자 조회
     * - 기업 상태 변경 시 기업 정지로 인해 정지된 관리자 복구용
     */
    List<Users> findByCompanyIdAndRoleInAndSuspendedByCompany(
            Long companyId,
            List<UserRole> roles,
            boolean suspendedByCompany);

    /**
     * 기업별 + 상태별 모든 사용자 조회 (역할 무관)
     * - Company 정지 시 모든 ACTIVE 사용자 정지용
     */
    List<Users> findByCompanyIdAndStatus(Long companyId, UserStatus status);

    /**
     * 기업별 + suspendedByCompany 플래그로 사용자 조회 (역할 무관)
     * - Company 재개 시 자동 정지된 모든 사용자 복구용
     */
    List<Users> findByCompanyIdAndSuspendedByCompany(
            Long companyId,
            Boolean suspendedByCompany
    );

    /**
     * 기업별 + 역할별 + 상태별 사용자 조회
     * - 기업 정지 시 ACTIVE 관리자만 정지용
     */
    List<Users> findByCompanyIdAndRoleInAndStatus(
            Long companyId,
            List<UserRole> roles,
            UserStatus status);

    /**
     * 기업별 + 역할별 사용자 수 카운트
     * - CompanyInfo 변환 시 매니저/일반사용자 수 카운트용
     */
    long countByCompanyIdAndRole(Long companyId, UserRole role);

    // ========== 배치용 메서드 ==========

    /**
     * 미활성 계정 조회 (배치용)
     * - 역할 + 상태 + 첫 로그인 여부 + 생성일 조건
     * - InactiveAccountCleanupScheduler에서 사용
     */
    List<Users> findByRoleInAndStatusAndIsFirstLoginAndCreatedAtBefore(
            List<UserRole> roles,
            UserStatus status,
            Boolean isFirstLogin,
            LocalDateTime createdAt
    );

    @Query("""
       SELECT COUNT(u) 
       FROM Users u 
       WHERE u.role = 'USER' AND YEAR(u.createdAt) = :year
       """)
    long countUsersByYear(@Param("year") int year);

    long countAllByRoleAndStatus(UserRole userRole, UserStatus userStatus);
}