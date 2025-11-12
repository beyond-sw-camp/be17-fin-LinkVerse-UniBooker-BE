package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.Resources;

import java.util.List;
import java.util.Optional;

public interface ResourcePersistencePort {

    // 리소스 생성
    void createResource(Resources resource);

    List<Resources> findAllByResourceGroupIdAndIsActiveTrueAndDeletedAtIsNull(Long serviceGroupId);

    Optional<Resources> findByIdAndIsActiveTrueAndDeletedAtIsNull(Long resourceId);

    void saveResource(Resources resource);

    void save(Resources resource);

    Optional<Resources> findById(Long resourceId);

    // 리소스 존재 여부 확인
    boolean existsById(Long resourceId);
}
