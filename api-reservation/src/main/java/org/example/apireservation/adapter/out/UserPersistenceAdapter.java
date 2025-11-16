package org.example.apireservation.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.entity.Users;
import org.example.apireservation.usecase.port.out.UserPersistencePort;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;

    @Override
    public Optional<Users> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Integer getTotalUserCountWithCompanyId(Long companyId, UserRole UserRole) {
        return userRepository.getTotalUserCountWithCompanyId(companyId, UserRole);
    }
}
