package org.example.apireservation.infrastructure;

import org.example.apireservation.adapter.out.external.ResourceInfo;

import java.util.Optional;

public interface ResourceExternalPort {
    // 상세 조회
    Optional<ResourceInfo> findById(Long resourceId);

    // 상세 조회 (활성화 & 미삭제 상태만)
    Optional<ResourceInfo> findResourceById(Long resourceId);

    // 상세 조회 (활성화 & 미삭제 상태 & 비관적 락)
    Optional<ResourceInfo> findResourceByIdForUpdate(Long resourceId);
}
