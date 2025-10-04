package org.example.unibooker.domain.user.repository;

import org.example.unibooker.domain.user.model.User;
import org.example.unibooker.domain.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 기업 ID로 사용자 목록 조회
     */
    List<User> findByCompanyId(Long companyId);

    /**
     * 기업 ID와 역할로 사용자 조회
     */
    Optional<User> findByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}