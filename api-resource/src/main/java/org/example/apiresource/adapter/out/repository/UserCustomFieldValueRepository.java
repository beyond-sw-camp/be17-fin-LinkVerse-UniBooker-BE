package org.example.apiresource.adapter.out.repository;

import org.example.apiresource.domain.model.entity.UserCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCustomFieldValueRepository extends JpaRepository<UserCustomFieldValues, Long> {
    List<UserCustomFieldValues> findByReservationIdAndDeletedAtIsNull(Long reservationId);

    Optional<UserCustomFieldValues> findByIdAndDeletedAtIsNull(Long id);
}