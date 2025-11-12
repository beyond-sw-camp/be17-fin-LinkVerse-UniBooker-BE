package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.ResourceCustomFieldValues;

import java.util.Optional;

public interface ResourceFieldValuePersistencePort {

    void save(ResourceCustomFieldValues resourceEntity);

    Optional<ResourceCustomFieldValues> findByIdAndDeletedAtIsNull(Long customFieldValueId);
}
