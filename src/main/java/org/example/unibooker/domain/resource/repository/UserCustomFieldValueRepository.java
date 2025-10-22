package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.UserCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCustomFieldValueRepository extends JpaRepository<UserCustomFieldValues, Long> {
    List<UserCustomFieldValues> findByReservationIdAndDeletedAtIsNull(Long reservationId);

    Optional<UserCustomFieldValues> findByIdAndDeletedAtIsNull(Long id);
}
