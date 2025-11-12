package org.example.apireservation.adapter.out.external;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.User;
import org.example.apireservation.infrastructure.UserExternalPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Circuit Breaker 통한 MSA 통신 */
@Component
@RequiredArgsConstructor
public class UserExternalAdapter implements UserExternalPort {
    @Override
    public Optional<User> findUserById(Long userId) {
        return null;
    }
}
