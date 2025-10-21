package org.example.unibooker.domain.resource.repository;

import org.example.unibooker.domain.resource.model.UserCustomFieldValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCustomFieldValueRepository extends JpaRepository<UserCustomFieldValues, Long> {
    List<UserCustomFieldValues> findByReservationIdAndDeletedAtIsNull(Long reservationId);
}
