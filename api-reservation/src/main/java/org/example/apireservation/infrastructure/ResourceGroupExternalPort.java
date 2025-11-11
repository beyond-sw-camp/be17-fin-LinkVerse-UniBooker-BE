package org.example.apireservation.infrastructure;

import org.example.apireservation.adapter.out.external.ResourceGroupInfo;

import java.util.Optional;

public interface ResourceGroupExternalPort {
    // 리소스 그룹 상세 조회
    Optional<ResourceGroupInfo> findByIdAndDeletedAtIsNull(Long resourceGroupId);
}
