package com.unibooker.main.domain.notification.repository;

import com.unibooker.main.domain.notification.model.entity.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 알림 레포지토리
 */
public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    /**
     * 사용자 알림 목록 조회 (최신순)
     */
    Page<Notifications> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 알림 조회 (사용자 확인)
     */
    Optional<Notifications> findByIdAndUserId(Long id, Long userId);

    /**
     * 읽지 않은 알림 개수
     */
    Long countByUserIdAndIsReadFalse(Long userId);
}