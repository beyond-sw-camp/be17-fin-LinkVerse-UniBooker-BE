package org.example.apireservation.infrastructure;

import org.example.apireservation.adapter.out.external.UserInfo;

import java.util.Optional;

public interface UserExternalPort {
    Optional<UserInfo> findUserById(Long userId);
}
