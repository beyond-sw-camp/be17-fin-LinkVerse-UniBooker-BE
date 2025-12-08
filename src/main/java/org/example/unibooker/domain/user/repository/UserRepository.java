package org.example.unibooker.domain.user.repository;

import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 레포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    // ========== 기본 조회 ==========

    /**
     * 이메일로 사용자 조회
     */
    Optional<Users> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 이메일로 모든 사용자 조회 (여러 기업에 가입한 경우)
     */
    List<Users> findAllByEmail(String email);

    // ========== 역할별 이메일 중복 확인 ==========

    /**
     * ADMIN과 MANAGER 통합 중복 체크
     * - ADMIN 회원가입 시: ADMIN, MANAGER와 중복 방지
     * - MANAGER 생성 시: ADMIN, MANAGER와 중복 방지
     */
    boolean existsByEmailAndRoleIn(String email, List<UserRole> roles);

    /**
     * 이메일과 권한 목록으로 사용자 조회
     */
    List<Users> findByEmailAndRoleIn(String email, List<UserRole> roles);

    /**
     * DELETED 상태가 아닌 사용자 중 이메일과 권한 목록으로 존재 여부 확인
     */
    boolean existsByEmailAndRoleInAndStatusNot(String email, List<UserRole> roles, UserStatus status);

    /**
     * 이메일과 권한 목록으로 사용자 조회 (DELETED 제외)
     */
    List<Users> findByEmailAndRoleInAndStatusNot(String email, List<UserRole> roles, UserStatus status);

    // ========== 기업별 조회 ==========

    /**
     * USER 중복 체크 (같은 기업 + USER role)
     */
    boolean existsByEmailAndCompany_IdAndRole(String email, Long companyId, UserRole role);

    /**
     * 특정 기업 내에서 이메일 존재 여부 확인
     */
    boolean existsByEmailAndCompany_Id(String email, Long companyId);

    /**
     * 기업과 권한으로 사용자 조회
     */
    Optional<Users> findByCompany_IdAndRole(Long companyId, UserRole role);

    /**
     * 기업과 권한으로 사용자 목록 조회
     */
    List<Users> findAllByCompany_IdAndRole(Long companyId, UserRole role);

    /**
     * 기업과 권한으로 사용자 페이징 조회
     */
    Page<Users> findByCompany_IdAndRole(Long companyId, UserRole role, Pageable pageable);

    /**
     * 기업과 여러 권한으로 사용자 목록 조회
     */
    List<Users> findByCompany_IdAndRoleIn(Long companyId, List<UserRole> roles);

    /**
     * 기업과 권한으로 사용자 수 조회
     */
    long countByCompany_IdAndRole(Long companyId, UserRole role);

    // ========== 상태별 조회 ==========

    /**
     * DELETED 상태가 아닌 사용자 중 이메일 존재 여부 확인
     */
    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    /**
     * 이메일과 상태로 사용자 조회
     */
    Optional<Users> findByEmailAndStatus(String email, UserStatus status);

    /**
     * 권한과 상태로 사용자 페이징 조회
     */
    Page<Users> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 권한으로 사용자 페이징 조회
     */
    Page<Users> findByRole(UserRole role, Pageable pageable);

    /**
     * 상태로 사용자 페이징 조회
     */
    Page<Users> findByStatus(UserStatus status, Pageable pageable);

    /**
     * 권한 목록으로 사용자 페이징 조회
     */
    Page<Users> findByRoleIn(List<UserRole> roles, Pageable pageable);

    // ========== 기업 + 상태 조합 조회 ==========

    /**
     * 이메일과 기업, 권한으로 사용자 조회 (DELETED 제외)
     */
    Optional<Users> findByEmailAndCompany_IdAndRoleAndStatusNot(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * DELETED 상태가 아닌 사용자 중 이메일과 기업, 권한으로 존재 여부 확인
     */
    boolean existsByEmailAndCompany_IdAndRoleAndStatusNot(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 이메일, 기업, 권한, 상태로 사용자 조회 (재가입 시 탈퇴 계정 찾기)
     */
    Optional<Users> findByEmailAndCompany_IdAndRoleAndStatus(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 기업과 권한으로 사용자 페이징 조회 (DELETED 제외)
     */
    Page<Users> findByCompany_IdAndRoleAndStatusNot(
            Long companyId,
            UserRole role,
            UserStatus status,
            Pageable pageable
    );

    // ========== 기업 정지 로직 ==========

    /**
     * 기업 정지로 인해 정지된 관리자 조회
     */
    List<Users> findByCompany_IdAndRoleInAndSuspendedByCompany(
            Long companyId,
            List<UserRole> roles,
            Boolean suspendedByCompany
    );

    /**
     * 특정 상태의 관리자 조회
     */
    List<Users> findByCompany_IdAndRoleInAndStatus(
            Long companyId,
            List<UserRole> roles,
            UserStatus status
    );

    // ========== 이메일 찾기 (이름 기반) ==========

    /**
     * 이름 + 기업 + 전화번호로 사용자 조회 (DELETED 제외)
     */
    Optional<Users> findByNameAndCompany_IdAndPhoneAndRoleAndStatusNot(
            String name,
            Long companyId,
            String phone,
            UserRole role,
            UserStatus status
    );

    /**
     * 이름 + 기업 + 생년월일로 사용자 조회 (DELETED 제외)
     */
    Optional<Users> findByNameAndCompany_IdAndBirthDateAndRoleAndStatusNot(
            String name,
            Long companyId,
            String birthDate,
            UserRole role,
            UserStatus status
    );

    // ========== 자동 삭제 대상 조회 ==========

    /**
     * 미활성 첫 로그인 계정 조회
     * - ADMIN/MANAGER 중 isFirstLogin=true
     * - 생성일이 특정 시간 이전
     */
    List<Users> findByRoleInAndStatusAndIsFirstLoginAndCreatedAtBefore(
            List<UserRole> roles,
            UserStatus status,
            Boolean isFirstLogin,
            LocalDateTime createdAtBefore
    );

    // ========== 통계 조회 ==========

    /**
     * 월별 누적 가입자 수 조회
     */
    int countAllByRoleAndCreatedAtBefore(UserRole role, LocalDateTime before);

    /**
     * 활성 상태인 특정 Role의 계정 수 조회
     */
    int countAllByRoleAndStatus(UserRole userRole, UserStatus status);

    /**
     * 특정 기업의 일반 사용자(USER) 수 조회
     */
    @Query("SELECT COUNT(u) FROM Users u WHERE u.company.id = :companyId AND u.role = 'USER'")
    Long countUsersByCompanyId(@Param("companyId") Long companyId);

    /**
     * 특정 기업의 최근 로그인 일시 조회
     */
    @Query("SELECT MAX(u.updatedAt) FROM Users u WHERE u.company.id = :companyId AND u.role = 'USER'")
    LocalDateTime findLastLoginByCompanyId(@Param("companyId") Long companyId);

    // ========== Fetch Join 조회 ==========

    /**
     * 사용자 조회 (Company Fetch Join)
     */
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.company WHERE u.id = :userId")
    Optional<Users> findByIdWithCompany(@Param("userId") Long userId);

    /**
     * 특정 기업의 모든 사용자 조회
     */
    List<Users> findByCompany_Id(Long companyId);

    // ========== 성별/연령대 통계 ==========

    /**
     * 기업별 성별 통계 조회
     */
    @Query("SELECT u.gender, COUNT(u) FROM Users u " +
            "WHERE u.company.id = :companyId " +
            "AND u.role = 'USER' " +
            "AND u.status != 'DELETED' " +
            "GROUP BY u.gender")
    List<Object[]> countByGenderAndCompanyId(@Param("companyId") Long companyId);

    /**
     * 기업별 USER 목록 조회 (연령대 계산용)
     */
    @Query("SELECT u FROM Users u " +
            "WHERE u.company.id = :companyId " +
            "AND u.role = 'USER' " +
            "AND u.status != 'DELETED' " +
            "AND u.birthDate IS NOT NULL")
    List<Users> findUsersWithBirthDateByCompanyId(@Param("companyId") Long companyId);
}