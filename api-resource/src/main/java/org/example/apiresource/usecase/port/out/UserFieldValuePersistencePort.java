package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.UserCustomFieldValues;

import java.util.Optional;

public interface UserFieldValuePersistencePort {

    void save(UserCustomFieldValues userEntity);

    Optional<UserCustomFieldValues> findByIdAndDeletedAtIsNull(Long valueId);
}
