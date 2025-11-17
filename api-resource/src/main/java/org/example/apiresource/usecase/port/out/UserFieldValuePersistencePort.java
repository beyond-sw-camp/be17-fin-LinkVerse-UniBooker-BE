package org.example.apiresource.usecase.port.out;

import org.example.apiresource.domain.model.entity.UserCustomFieldValues;

import java.util.List;
import java.util.Optional;

public interface UserFieldValuePersistencePort {

    UserCustomFieldValues save(UserCustomFieldValues userEntity);

    Optional<UserCustomFieldValues> findByIdAndDeletedAtIsNull(Long valueId);

    List<UserCustomFieldValues> findByReservationIdAndDeletedAtIsNull(Long reservationId);
}