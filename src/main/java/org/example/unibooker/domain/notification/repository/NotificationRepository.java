package org.example.unibooker.domain.notification.repository;

import org.example.unibooker.domain.notification.model.entity.Notifications;
import org.example.unibooker.domain.user.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    Page<Notifications> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
