package org.example.apireservation.infrastructure;

import org.example.apireservation.domain.model.ResourceGroup;

import java.util.Optional;

public interface ResourceGroupExternalPort {
    // 리소스 그룹 상세 조회
    Optional<ResourceGroup> findByIdAndDeletedAtIsNull(Long resourceGroupId);
}
