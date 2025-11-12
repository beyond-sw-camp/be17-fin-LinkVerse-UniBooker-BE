package org.example.apireservation.infrastructure;

import org.example.apireservation.domain.model.Resource;

import java.util.Optional;

public interface ResourceExternalPort {
    // 상세 조회
    Optional<Resource> findById(Long resourceId);

    // 상세 조회 (활성화 & 미삭제 상태만)
    Optional<Resource> findResourceById(Long resourceId);

    // 상세 조회 (활성화 & 미삭제 상태 & 비관적 락)
    Optional<Resource> findResourceByIdForUpdate(Long resourceId);
}
