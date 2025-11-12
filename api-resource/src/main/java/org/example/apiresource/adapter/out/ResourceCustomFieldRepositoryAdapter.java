package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.ResourceCustomFieldValueRepository;
import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.example.apiresource.usecase.port.out.ResourceCustomFieldPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceCustomFieldRepositoryAdapter implements ResourceCustomFieldPersistencePort {

    private final ResourceCustomFieldValueRepository resourceCustomFieldValueRepository;

    @Override
    @Transactional
    public void saveAll(List<ResourceCustomFieldValues> customFieldValues) {
        resourceCustomFieldValueRepository.saveAll(customFieldValues);
    }


    // 모든 리소스 커스텀 필드 값 조회
    @Override
    @Transactional
    public List<ResourceCustomFieldValues> findByResourceIdAndDeletedAtIsNull(Long resourceId) {
        return resourceCustomFieldValueRepository.findByResourceIdAndDeletedAtIsNull(resourceId);
    }
}
