package org.example.unibooker.domain.user.repository;

import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.UserStatus;
import org.example.unibooker.domain.user.model.entity.User;
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
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 기업 ID와 권한으로 사용자 조회
     */
    Optional<User> findByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 기업 ID와 권한으로 사용자 페이징 조회
     */
    Page<User> findByCompanyIdAndRole(Long companyId, UserRole role, Pageable pageable);

    /**
     * 권한과 상태로 사용자 페이징 조회
     */
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 권한으로 사용자 페이징 조회
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * 상태로 사용자 페이징 조회
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * 권한 목록으로 사용자 페이징 조회
     */
    Page<User> findByRoleIn(List<UserRole> roles, Pageable pageable);
}