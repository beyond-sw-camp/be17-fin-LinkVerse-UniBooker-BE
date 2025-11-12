package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;
import org.example.apiresource.domain.model.entity.UserCustomFieldValues;
import org.example.apiresource.usecase.port.out.ResourceFieldValuePersistencePort;
import org.example.apiresource.usecase.port.out.UserFieldValuePersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceFieldValuePersistenceAdapter implements ResourceFieldValuePersistencePort {

    private final ResourceFieldValuePersistencePort resourceFieldValuePersistencePort;

    @Override
    @Transactional
    public void save(ResourceCustomFieldValues resourceEntity) {
        resourceFieldValuePersistencePort.save(resourceEntity);
    }

    @Override
    @Transactional
    public Optional<ResourceCustomFieldValues> findByIdAndDeletedAtIsNull(Long customFieldValueId) {
        return Optional.empty();
    }
}
