package org.example.apireservation.infrastructure;

import org.example.apireservation.domain.model.User;

import java.util.Optional;

public interface UserExternalPort {
    Optional<User> findUserById(Long userId);
}
