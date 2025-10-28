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

    /**
     * 이메일로 사용자 조회
     */
    Optional<Users> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    // ========== 역할별 이메일 중복 확인 (추가) ==========

    /**
     * ADMIN과 MANAGER 통합 중복 체크
     * - ADMIN 회원가입 시: ADMIN, MANAGER와 중복 방지
     * - MANAGER 생성 시: ADMIN, MANAGER와 중복 방지
     */
    boolean existsByEmailAndRoleIn(String email, List<UserRole> roles);

    /**
     * 이메일과 권한 목록으로 사용자 조회
     * - ADMIN 회원가입 상태 조회 시 사용
     */
    List<Users> findByEmailAndRoleIn(String email, List<UserRole> roles);

    /**
     * USER 중복 체크 (같은 Company + USER role)
     * - 같은 회사 내에서만 USER 이메일 고유성 보장
     */
    boolean existsByEmailAndCompanyIdAndRole(String email, Long companyId, UserRole role);

    // ========== 기업별 이메일 중복 확인 ==========

    /**
     * 이메일과 기업 ID로 사용자 조회
     */
    Optional<Users> findByEmailAndCompanyIdAndRoleAndStatusNot(
            String email,
            Long companyId,
            UserRole role,
            UserStatus status
    );

    /**
     * 특정 기업 내에서 이메일 존재 여부 확인
     */
    boolean existsByEmailAndCompanyId(String email, Long companyId);

    /**
     * 이메일로 모든 사용자 조회 (여러 기업에 가입한 경우)
     */
    List<Users> findAllByEmail(String email);

    /**
     * 기업 ID와 권한으로 사용자 조회
     */
    Optional<Users> findByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 기업 ID와 권한으로 사용자 목록 조회
     */
    List<Users> findAllByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 기업 ID와 권한으로 사용자 페이징 조회
     */
    Page<Users> findByCompanyIdAndRole(Long companyId, UserRole role, Pageable pageable);

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

    /**
     * DELETED 상태가 아닌 사용자 중 이메일 존재 여부 확인
     */
    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    /**
     * DELETED 상태가 아닌 사용자 중 이메일과 기업 ID, 권한으로 존재 여부 확인
     */
    boolean existsByEmailAndCompanyIdAndRoleAndStatusNot(String email, Long companyId, UserRole role, UserStatus status);

    /**
     * DELETED 상태가 아닌 사용자 중 이메일과 권한 목록으로 존재 여부 확인
     */
    boolean existsByEmailAndRoleInAndStatusNot(String email, List<UserRole> roles, UserStatus status);

    /**
     * 이메일과 상태로 사용자 조회 (재가입 시 탈퇴 계정 찾기용)
     */
    Optional<Users> findByEmailAndStatus(String email, UserStatus status);

    /**
     * 이메일, 기업 ID, 권한, 상태로 사용자 조회 (재가입 시 탈퇴 계정 찾기용)
     */
    Optional<Users> findByEmailAndCompanyIdAndRoleAndStatus(String email, Long companyId, UserRole role, UserStatus status);

    /**
     * 이름 + 기업ID + 전화번호로 사용자 조회 (USER 역할, DELETED 제외)
     */
    Optional<Users> findByNameAndCompanyIdAndPhoneAndRoleAndStatusNot(
            String name,
            Long companyId,
            String phone,
            UserRole role,
            UserStatus status
    );

    /**
     * 이름 + 기업ID + 생년월일로 사용자 조회 (USER 역할, DELETED 제외)
     */
    Optional<Users> findByNameAndCompanyIdAndBirthDateAndRoleAndStatusNot(
            String name,
            Long companyId,
            String birthDate,
            UserRole role,
            UserStatus status
    );

    /**
     * 이메일과 권한 목록으로 사용자 조회 (DELETED 제외)
     * - ADMIN/MANAGER/SUPER 로그인 시 사용
     */
    List<Users> findByEmailAndRoleInAndStatusNot(String email, List<UserRole> roles, UserStatus status);

    /**
     * 미활성 첫 로그인 계정 조회 (자동 삭제 대상)
     * - ADMIN/MANAGER 중 isFirstLogin=true
     * - status=ACTIVE (INACTIVE, SUSPENDED, DELETED 제외)
     * - 생성일이 특정 시간 이전
     */
    List<Users> findByRoleInAndStatusAndIsFirstLoginAndCreatedAtBefore(
            List<UserRole> roles,
            UserStatus status,
            Boolean isFirstLogin,
            LocalDateTime createdAtBefore
    );

    /**
     * 기업 ID와 권한으로 사용자 페이징 조회 (DELETED 제외)
     */
    Page<Users> findByCompanyIdAndRoleAndStatusNot(
            Long companyId,
            UserRole role,
            UserStatus status,
            Pageable pageable
    );

    // 월별 누적 가입자 수 조회
    int countAllByRoleAndCreatedAtBefore(UserRole role, LocalDateTime before);
    // 활성 상태인 특정 Role의 계정 수 조회
    int countAllByRoleAndStatus(UserRole userRole, UserStatus status);

    /**
     * 기업 ID와 여러 권한으로 사용자 목록 조회
     * - 기업 정지 시 소속 ADMIN/MANAGER 일괄 정지용
     */
    List<Users> findByCompanyIdAndRoleIn(Long companyId, List<UserRole> roles);

    /**
     * 기업 ID와 권한으로 사용자 수 조회
     */
    long countByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 특정 기업의 일반 사용자(USER) 수 조회
     */
    @Query("SELECT COUNT(u) FROM Users u WHERE u.companyId = :companyId AND u.role = 'USER'")
    Long countUsersByCompanyId(@Param("companyId") Long companyId);

    /**
     * 특정 기업의 최근 로그인 일시 조회
     */
    @Query("SELECT MAX(u.updatedAt) FROM Users u WHERE u.companyId = :companyId AND u.role = 'USER'")
    LocalDateTime findLastLoginByCompanyId(@Param("companyId") Long companyId);
}