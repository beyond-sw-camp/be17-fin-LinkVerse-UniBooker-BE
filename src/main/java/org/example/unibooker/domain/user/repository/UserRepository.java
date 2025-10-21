package org.example.unibooker.domain.user.repository;

import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    Optional<Users> findByEmailAndCompanyId(String email, Long companyId);

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
}