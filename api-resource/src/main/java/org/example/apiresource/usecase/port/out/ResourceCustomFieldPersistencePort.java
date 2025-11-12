package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;

import java.util.List;

public interface ResourceCustomFieldPersistencePort {

    void saveAll(List<ResourceCustomFieldValues> customFieldValues);

    // 모든 리소스 커스텀 필드 값 조회
    List<ResourceCustomFieldValues> findByResourceIdAndDeletedAtIsNull(Long resourceId);
}
