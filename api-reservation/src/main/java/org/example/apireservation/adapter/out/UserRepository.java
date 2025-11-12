package org.example.apireservation.adapter.out;

import org.example.apireservation.domain.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
}
