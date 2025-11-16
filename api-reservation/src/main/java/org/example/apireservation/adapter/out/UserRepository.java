package org.example.apireservation.adapter.out;

import org.example.apireservation.domain.model.entity.Users;
import org.example.common.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Users, Long> {
    // 기업에 속한 사용자 수 (관리자 제외)
    @Query("SELECT count(u) FROM Users u WHERE u.companyId=:companyId AND u.role=:UserRole")
    Integer getTotalUserCountWithCompanyId(Long companyId, UserRole UserRole);
}
