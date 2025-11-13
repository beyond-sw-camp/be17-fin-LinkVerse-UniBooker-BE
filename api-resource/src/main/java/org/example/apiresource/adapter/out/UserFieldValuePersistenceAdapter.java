package org.example.apiresource.adapter.out;

import lombok.RequiredArgsConstructor;
import org.example.apiresource.adapter.out.repository.UserCustomFieldValueRepository;
import org.example.apiresource.domain.model.entity.UserCustomFieldValues;
import org.example.apiresource.usecase.port.out.UserFieldValuePersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFieldValuePersistenceAdapter implements UserFieldValuePersistencePort {

    private final UserCustomFieldValueRepository userFieldValueRepository;

    @Override
    @Transactional
    public UserCustomFieldValues save(UserCustomFieldValues userEntity) {
        return userFieldValueRepository.save(userEntity);
    }


    @Override
    @Transactional
    public Optional<UserCustomFieldValues> findByIdAndDeletedAtIsNull(Long valueId) {
        return userFieldValueRepository.findByIdAndDeletedAtIsNull(valueId);
    }


    @Override
    @Transactional
    public List<UserCustomFieldValues> findByReservationIdAndDeletedAtIsNull(Long reservationId) {
        return userFieldValueRepository.findByReservationIdAndDeletedAtIsNull(reservationId);
    }
}
