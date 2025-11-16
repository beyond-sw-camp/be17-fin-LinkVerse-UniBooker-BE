package org.example.apireservation.usecase.port.out;

import org.example.apireservation.domain.model.entity.Users;
import org.example.common.model.UserRole;

import java.util.Optional;

public interface UserPersistencePort {

    // 사용자 상세 조회
    Optional<Users> findById(Long userId);

    // 기업에 속한 사용자 수 (관리자 제외)
    Integer getTotalUserCountWithCompanyId(Long companyId, UserRole UserRole);
}
