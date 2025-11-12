package org.example.apireservation.usecase.port.out;

import org.example.apireservation.domain.model.entity.Users;

import java.util.Optional;

public interface UserPersistencePort {

    // 사용자 상세 조회
    Optional<Users> findById(Long userId);
}
